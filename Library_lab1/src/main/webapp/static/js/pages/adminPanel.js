import { initializeSelects, fillSelect } from '../utils/adminUtils.js';
import { tableConfigs, modalConfigs, quickModalConfig } from '../services/adminConfig.js';
import { BookItemApi } from '../api/bookItemApi.js';
import { PaginationHelper } from '../utils/utils.js';

const navItems = document.querySelectorAll(".nav-item");
const overlay = document.getElementById("modal-overlay");
const quickModal = document.getElementById("modal-quick-add");
const openBtn = document.getElementById("open-add-form");

// for limiting a number of table entries (to not overload)
let allData = [];
let displayedCount = 0;
const PAGE_SIZE = 20; // limit for one portion

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

// main modal windows logic
async function openModal(action) {
  const config = modalConfigs[action];
  if (!config) return;

  document.getElementById("modal-title").textContent = config.title;
  const formContainer = document.getElementById("admin-form");
  formContainer.innerHTML = config.renderer();

  overlay.classList.add("active");

  if (action === 'add-book') {
      try {
          await Promise.all([
              fillSelect('#author-select', tableConfigs["authors-section"].apiCall, 'id', 'penName'),
              fillSelect('#genre-select', tableConfigs["genres-section"].apiCall, 'id', 'name')
          ]);
      } catch (err) {
          console.error("Помилка завантаження даних у форму", err);
      }
  }
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

    const loadMoreBtn = document.querySelector(".load-more");
        loadMoreBtn.addEventListener("click", () => {
            if (currentRenderer) {
                renderNextChunk(currentRenderer);
            }
        });

}

document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners();
    switchSection("books-section"); // first section
    async function init() {
        const data = await BookItemApi.getAll();
        data.forEach(d => console.log(d));
    }
    init();
});

async function loadData(targetId) {
	const tableBody = document.querySelector(".table-body");
    const config = tableConfigs[targetId];
    const loadMoreBtn = document.querySelector(".load-more");
    if (!config || !config.apiCall) {
		console.error(`No API configuration for section: ${targetId}`);
        return;
    }

    displayedCount = 0;
    allData = [];
    tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading">Loading...</td></tr>';
    loadMoreBtn.style.display = "none";
    try{
        allData = await config.apiCall();
        tableBody.innerHTML = "";
        if (!allData || allData.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading not-found">No data found</td></tr>';
            return;
        }
        renderNextChunk(config.renderer);
    }
    catch(error){
        console.error("Failed to load data:", error);
        tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading error"">Error loading data</td></tr>';
    }
    };

function renderNextChunk() {
    const tableBody = document.querySelector(".table-body");
    const loadMoreBtn = document.querySelector(".load-more");
    const nextChunk = PaginationHelper.getNextChunk(allData, displayedCount, PAGE_SIZE);
    nextChunk.forEach(item => {
        tableBody.insertAdjacentHTML("beforeend", currentRenderer(item));
    });
    displayedCount += nextChunk.length;
    loadMoreBtn.style.display = PaginationHelper.hasMore(allData, displayedCount) ? "block" : "none";
}