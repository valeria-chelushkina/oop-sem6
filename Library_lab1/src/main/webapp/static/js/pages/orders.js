import { getOrdersConfig } from '../services/ordersConfig.js';
import { PaginationHelper } from '../utils/utils.js';
import { LoanApi } from '../api/loanApi.js';

const globalSearchInput = document.getElementById("global-search");
const dynamicFiltersContainer = document.getElementById("dynamic-filters-container");
const tableBody = document.querySelector(".table-body");
const loadMoreBtn = document.querySelector(".load-more");
const tableHead = document.querySelector(".table-head");
const tabTitle = document.getElementById("tab-title");
const pageTitle = document.getElementById("page-title");

const actionModal = document.getElementById("action-modal");
const actionModalTitle = document.getElementById("action-modal-title");
const actionModalBody = document.getElementById("action-modal-body");
const actionSubmitBtn = document.getElementById("action-submit-btn");

let allData = [];
let filteredData = [];
let displayedCount = 0;
let ordersConfig = null;
let currentAuth = null;
const PAGE_SIZE = 20;

function readSectionFiltersFromDom() {
    const filters = {};
    if (!dynamicFiltersContainer) return filters;
    
    dynamicFiltersContainer.querySelectorAll(".admin-filter-control").forEach((el) => {
        if (!el.name) return;
        if (el.type === "checkbox") {
            if (!el.checked) return;
            if (!Array.isArray(filters[el.name])) filters[el.name] = [];
            filters[el.name].push(el.value);
        } else if (el.value != null && el.value !== "") {
            filters[el.name] = el.value;
        }
    });
    return filters;
}

function applyFiltersAndRender() {
    const query = globalSearchInput?.value?.trim() ?? "";
    const filters = readSectionFiltersFromDom();
    const filterLogic = ordersConfig.filterLogic;
    
    if (typeof filterLogic === "function") {
        filteredData = allData.filter((item) => filterLogic(item, query, filters));
    } else {
        filteredData = allData;
    }

    tableBody.innerHTML = "";
    displayedCount = 0;
    if (!filteredData.length) {
        tableBody.innerHTML = `<tr><td colspan="100%" class="table-loading not-found">No orders found</td></tr>`;
        loadMoreBtn.style.display = "none";
    } else {
        renderNextChunk();
    }
}

function renderNextChunk() {
    const nextChunk = PaginationHelper.getNextChunk(filteredData, displayedCount, PAGE_SIZE);
    nextChunk.forEach(item => {
        tableBody.insertAdjacentHTML("beforeend", ordersConfig.renderer(item));
    });
    displayedCount += nextChunk.length;
    loadMoreBtn.style.display = PaginationHelper.hasMore(filteredData, displayedCount) ? "block" : "none";
}

function renderTableHeader() {
    if (tableHead) {
        tableHead.innerHTML = ordersConfig.headers
            .map(header => `<th>${header}</th>`)
            .join("");
    }
    tabTitle.textContent = ordersConfig.title;
    pageTitle.textContent = ordersConfig.title;
}

function renderDynamicFilters() {
    if (!dynamicFiltersContainer || typeof ordersConfig.filterTemplate !== "function") return;
    dynamicFiltersContainer.innerHTML = ordersConfig.filterTemplate();
}

function openModal(title, content, onSubmit) {
    actionModalTitle.textContent = title;
    actionModalBody.innerHTML = content;
    actionSubmitBtn.onclick = onSubmit;
    actionModal.classList.add("active");
}

function closeModal() {
    actionModal.classList.remove("active");
    actionModalBody.innerHTML = '';
}

async function handleAction(e) {
    const btn = e.target.closest(".action-btn");
    if (!btn) return;

    const id = btn.dataset.id;
    const loan = allData.find(l => String(l.id) === id);

    if (btn.classList.contains("issue-btn")) {
        const today = new Date().toISOString().split('T')[0];
        const defaultDue = new Date();
        defaultDue.setDate(defaultDue.getDate() + 14);
        const defaultDueStr = defaultDue.toISOString().split('T')[0];

        const content = `
            <div class="form-group">
                <label>Due Date:</label>
                <input type="date" id="issue-due-date" value="${defaultDueStr}" min="${today}" required>
            </div>
        `;
        openModal("Issue Order", content, async () => {
            const dueDate = document.getElementById("issue-due-date").value;
            if (!dueDate) return alert("Please select a due date");
            
            try {
                const now = new Date();
                const formattedNow = now.getFullYear() + '-' + 
                    String(now.getMonth() + 1).padStart(2, '0') + '-' + 
                    String(now.getDate()).padStart(2, '0') + ' ' + 
                    String(now.getHours()).padStart(2, '0') + ':' + 
                    String(now.getMinutes()).padStart(2, '0');

                const updatedLoan = { 
                    ...loan, 
                    status: 'ISSUED', 
                    dueDate: dueDate,
                    librarianId: currentAuth.id,
                    loanDate: formattedNow // Backend expects yyyy-MM-dd HH:mm
                };
                await LoanApi.update(updatedLoan);
                closeModal();
                await reloadData();
            } catch (err) {
                alert("Failed to issue order: " + err.message);
            }
        });
    } else if (btn.classList.contains("return-btn")) {
        const content = `
            <div class="form-group">
                <label>Final Status:</label>
                <select id="return-status">
                    <option value="RETURNED">Returned</option>
                    <option value="LOST">Lost</option>
                    <option value="DAMAGED">Damaged</option>
                </select>
            </div>
        `;
        openModal("Return Order", content, async () => {
            const status = document.getElementById("return-status").value;
            try {
                const now = new Date();
                const formattedNow = now.getFullYear() + '-' + 
                    String(now.getMonth() + 1).padStart(2, '0') + '-' + 
                    String(now.getDate()).padStart(2, '0') + ' ' + 
                    String(now.getHours()).padStart(2, '0') + ':' + 
                    String(now.getMinutes()).padStart(2, '0');

                const updatedLoan = { ...loan, status: status };
                if (status === 'RETURNED' || status === 'DAMAGED') {
                    updatedLoan.returnDate = formattedNow;
                }
                await LoanApi.update(updatedLoan);
                closeModal();
                await reloadData();
            } catch (err) {
                alert("Failed to process return: " + err.message);
            }
        });
    } else if (btn.classList.contains("delete-btn")) {
        if (confirm(`Cancel and delete order #${id}?`)) {
            try {
                await LoanApi.delete(id);
                await reloadData();
            } catch (err) {
                alert("Failed to delete order: " + err.message);
            }
        }
    }
}

async function reloadData() {
    tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading">Refreshing...</td></tr>';
    try {
        allData = await ordersConfig.apiCall(currentAuth.id);
        if (currentAuth.role !== 'LIBRARIAN') {
            allData.sort((a, b) => new Date(b.loanDate) - new Date(a.loanDate));
        }
        applyFiltersAndRender();
    } catch (error) {
        console.error("Failed to reload:", error);
    }
}

function bindListeners() {
    globalSearchInput?.addEventListener("input", applyFiltersAndRender);
    dynamicFiltersContainer?.addEventListener("input", (e) => {
        if (e.target.classList.contains("admin-filter-control")) applyFiltersAndRender();
    });
    dynamicFiltersContainer?.addEventListener("change", (e) => {
        if (e.target.classList.contains("admin-filter-control")) applyFiltersAndRender();
    });
    loadMoreBtn?.addEventListener("click", renderNextChunk);
    tableBody?.addEventListener("click", handleAction);
    
    document.getElementById("close-action-modal").onclick = closeModal;
    document.getElementById("action-cancel-btn").onclick = closeModal;
}

async function init() {
    const statusResponse = await fetch('/api/auth/status');
    currentAuth = await statusResponse.json();
    if (!currentAuth.authenticated) {
        window.location.assign('/login');
        return;
    }

    const isLibrarian = currentAuth.role === 'LIBRARIAN';
    ordersConfig = getOrdersConfig(isLibrarian);

    if(isLibrarian){
        const optionsList = document.getElementById('options-list');
    	optionsList.innerHTML += '<li class="profile-nav-item"><a href="/management">Management</a></li>'
    }

    renderTableHeader();
    renderDynamicFilters();
    bindListeners();
    
    tableBody.innerHTML = `<tr><td colspan="100%" class="table-loading">Loading ${isLibrarian ? 'ongoing ' : 'your '}orders...</td></tr>`;
    
    try {
        allData = await ordersConfig.apiCall(currentAuth.id);
        if (!isLibrarian) {
            allData.sort((a, b) => new Date(b.loanDate) - new Date(a.loanDate));
        }
        applyFiltersAndRender();
    } catch (error) {
        console.error("Failed to load orders:", error);
        tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading error">Error loading orders</td></tr>';
    }
}

document.addEventListener("DOMContentLoaded", init);
