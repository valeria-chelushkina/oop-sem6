import { initializeSelects } from '../utils/adminUtils.js';
import { tableConfigs, modalConfigs, quickModalConfig } from '../services/adminConfig.js';

const navItems = document.querySelectorAll(".nav-item");
const overlay = document.getElementById("modal-overlay");
const quickModal = document.getElementById("modal-quick-add");
const openBtn = document.getElementById("open-add-form");
let currentRenderer = null;

// navigation and section switch
function switchSection(targetId) {
    navItems.forEach(nav => nav.classList.remove("active"));
    const activeNav = document.querySelector(`[data-target="${targetId}"]`);
    if (activeNav) activeNav.classList.add("active");

    const config = tableConfigs[targetId];
    if (!config) return;

    currentRenderer = config.renderer;
    renderTableHeader(config);
    loadData(targetId);
}

function renderTableHeader(config) {
    const tableHead = document.querySelector(".table-head");
    if (tableHead) {
        tableHead.innerHTML = config.headers
            .map(header => `<th>${header}</th>`)
            .join("");
    }
    document.getElementById("tab-title").textContent = config.title;
    openBtn.dataset.action = config.action;
}

function loadData(targetId) {
    // temporary data - will be changed
    const fakeData = {
        "books-section": [
            { id: 1, title: "The Hobbit", ISBN: "123456", authors: [{ penName: "Tolkien" }] },
        ],
        "authors-section": [
            { id: 1, penName: "Tolkien" },
        ],
    };

    const data = fakeData[targetId] || [];
    const tableBody = document.querySelector(".table-body");
    tableBody.innerHTML = "";

    data.forEach(item => {
        if (currentRenderer) {
            tableBody.insertAdjacentHTML("beforeend", currentRenderer(item));
        }
    });
}

// main modal windows logic
function openModal(action) {
    const config = modalConfigs[action];
    if (!config) return;

    document.getElementById("modal-title").textContent = config.title;
    document.getElementById("admin-form").innerHTML = config.renderer();
    overlay.classList.add("active");

    initializeSelects();
}

function closeModal() {
    overlay.classList.remove("active");
}

// quick modal windows logic
function openModalQuick(action) {
    const config = quickModalConfig[action];
    if (!config) return;

    overlay.classList.add("blocked");
    document.getElementById("quick-modal-title").textContent = config.title;
    document.getElementById("quick-admin-form").innerHTML = config.renderer();
    quickModal.classList.add("active");

    initializeSelects();
}

function closeQuickModal() {
    quickModal.classList.remove("active");
    overlay.classList.remove("blocked");
}

// event listeners initialization
function setupEventListeners() {
    // navigation
    navItems.forEach(item => {
        item.addEventListener("click", () => switchSection(item.dataset.target));
    });

    // new modal open
    openBtn.addEventListener("click", (e) => {
        openModal(e.currentTarget.dataset.action);
    });

    [overlay, quickModal].forEach(m => {
        if (!m) return;
        m.addEventListener('click', (e) => {
            if (e.target.closest('.close-btn')) {
                m === quickModal ? closeQuickModal() : closeModal();
            }
        });
    });

    // quick add buttons events
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-quick-add");
        if (btn) {
            e.preventDefault();
            openModalQuick(btn.dataset.action);
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners();
    switchSection("books-section"); // first section
});