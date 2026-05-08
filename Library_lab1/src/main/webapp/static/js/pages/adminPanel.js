import { initializeSelects, fillSelect } from '../utils/adminUtils.js';
import { tableConfigs, modalConfigs, quickModalConfig } from '../services/adminConfig.js';
import { PaginationHelper } from '../utils/utils.js';
import { openEditForSection, saveForSection, deleteForSection } from "../services/adminCrudRegistry.js";

const navItems = document.querySelectorAll(".nav-item");
const overlay = document.getElementById("modal-overlay");
const quickModal = document.getElementById("modal-quick-add");
const openBtn = document.getElementById("open-add-form");
const globalSearchInput = document.getElementById("global-search");
const dynamicFiltersContainer = document.getElementById("dynamic-filters-container");

// for limiting a number of table entries (to not overload)
let allData = [];
let filteredData = [];
let displayedCount = 0;
const PAGE_SIZE = 20; // limit for one portion

let currentRenderer = null;
let currentTargetId = "books-section";
let currentModalContext = { section: null, mode: "create", id: null };

function readSectionFiltersFromDom() {
    const filters = {};
    if (!dynamicFiltersContainer) {
        return filters;
    }
    dynamicFiltersContainer.querySelectorAll(".admin-filter-control").forEach((el) => {
        if (!el.name) {
            return;
        }
        if (el.type === "checkbox") {
            if (!el.checked) {
                return;
            }
            if (!Array.isArray(filters[el.name])) {
                filters[el.name] = [];
            }
            filters[el.name].push(el.value);
            return;
        }
        if (el.value != null && el.value !== "") {
            filters[el.name] = el.value;
        }
    });
    return filters;
}

function buildAdminParams(targetId = currentTargetId) {
    const params = new URLSearchParams();
    params.set("section", targetId);
    const query = globalSearchInput?.value?.trim();
    if (query) {
        params.set("query", query);
    }
    const filters = readSectionFiltersFromDom();
    Object.entries(filters).forEach(([key, value]) => {
        if (Array.isArray(value)) {
            value.forEach((v) => params.append(key, v));
        } else {
            params.set(key, value);
        }
    });
    return params;
}

function syncStateToUrl(targetId = currentTargetId) {
    const params = buildAdminParams(targetId);
    const qs = params.toString();
    const path = window.location.pathname;
    window.history.replaceState({}, "", qs ? `${path}?${qs}` : path);
}

function applyFiltersAndRender() {
    const config = tableConfigs[currentTargetId];
    const query = globalSearchInput?.value?.trim() ?? "";
    const filters = readSectionFiltersFromDom();
    const filterLogic = config?.filterLogic;
    if (typeof filterLogic === "function") {
        filteredData = allData.filter((item) => filterLogic(item, query, filters));
    } else {
        filteredData = allData;
    }

    const tableBody = document.querySelector(".table-body");
    const loadMoreBtn = document.querySelector(".load-more");
    tableBody.innerHTML = "";
    displayedCount = 0;
    if (!filteredData.length) {
        tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading not-found">No data found</td></tr>';
        loadMoreBtn.style.display = "none";
    } else {
        renderNextChunk();
    }
    syncStateToUrl();
}

function renderDynamicFilters(targetId) {
    if (!dynamicFiltersContainer) {
        return;
    }
    const config = tableConfigs[targetId];
    if (!config || typeof config.filterTemplate !== "function") {
        dynamicFiltersContainer.innerHTML = "";
        return;
    }
    dynamicFiltersContainer.innerHTML = config.filterTemplate();
}

function applyUrlStateToControls() {
    const params = new URLSearchParams(window.location.search);
    if (globalSearchInput) {
        globalSearchInput.value = params.get("query") || "";
    }
    if (!dynamicFiltersContainer) {
        return;
    }
    dynamicFiltersContainer.querySelectorAll(".admin-filter-control").forEach((el) => {
        if (!el.name) {
            return;
        }
        if (el.type === "checkbox") {
            el.checked = params.getAll(el.name).includes(el.value);
        } else {
            el.value = params.get(el.name) || "";
        }
    });
}

// navigation and section switch
function switchSection(targetId) {
    navItems.forEach(nav => nav.classList.remove("active"));
    const activeNav = document.querySelector(`[data-target="${targetId}"]`);
    if (activeNav) activeNav.classList.add("active");

    const config = tableConfigs[targetId];
    if (!config) return;

    currentTargetId = targetId;
    currentRenderer = config.renderer;
    renderTableHeader(config);
    renderDynamicFilters(targetId);
    applyUrlStateToControls();
    bindDynamicFilterListeners();
    syncStateToUrl(targetId);
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
  currentModalContext = { section: currentTargetId, mode: "create", id: null };

  overlay.classList.add("active");

  if (action === 'add-book') {
      try {
          await Promise.all([
              fillSelect('#author-select', tableConfigs["authors-section"].apiCall, 'id', 'penName'),
              fillSelect('#genre-select', tableConfigs["genres-section"].apiCall, 'id', 'name')
          ]);
      } catch (err) {
          console.error("Error loading data to the form", err);
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

    const adminForm = document.getElementById("admin-form");

    adminForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      await saveForSection({
        sectionId: currentModalContext.section,
        mode: currentModalContext.mode,
        id: currentModalContext.id,
        formEl: adminForm,
      });
      closeModal();
      await loadData(currentTargetId);
    });

    const tableBody = document.querySelector(".table-body");
    tableBody?.addEventListener("click", async (e) => {
        const clickedViewUpdate = Boolean(e.target.closest(".view-update-btn"));
        const clickedDelete = Boolean(e.target.closest(".delete-btn"));

        const row = e.target.closest("tr[data-entry-id]");
        if (!row) {
            return;
        }
        const id = Number(row.dataset.entryId);
        if (!Number.isFinite(id)) {
            return;
        }

        // view/update
        if (clickedViewUpdate) {
            try {
                await openEditForSection({
                    sectionId: currentTargetId,
                    id,
                    overlayEl: overlay,
                    titleEl: document.getElementById("modal-title"),
                    formEl: document.getElementById("admin-form"),
                    setModalContext: (ctx) => (currentModalContext = ctx),
                });
            } catch (err) {
                console.error("Open edit failed:", err);
                alert(err?.message || "Open edit failed.");
            }
            return;
        }

        // delete
        if (clickedDelete) {
            if (!window.confirm(`Delete entry #${id}?`)) {
                return;
            }
            try {
                await deleteForSection({ sectionId: currentTargetId, id });
                await loadData(currentTargetId);
            } catch (err) {
                console.error("Delete failed:", err);
                alert(err?.message || "Delete failed.");
            }
        }
			});



    const loadMoreBtn = document.querySelector(".load-more");
        loadMoreBtn.addEventListener("click", () => {
            if (currentRenderer) {
                renderNextChunk(currentRenderer);
            }
        });

    if (globalSearchInput) {
        globalSearchInput.addEventListener("input", () => {
            applyFiltersAndRender();
        });
    }

}

function bindDynamicFilterListeners() {
    if (!dynamicFiltersContainer) {
        return;
    }
    dynamicFiltersContainer.querySelectorAll(".admin-filter-control").forEach((el) => {
        el.addEventListener("input", applyFiltersAndRender);
        el.addEventListener("change", applyFiltersAndRender);
    });
}

document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners();
    const sectionFromUrl = new URLSearchParams(window.location.search).get("section");
    const firstSection = sectionFromUrl && tableConfigs[sectionFromUrl] ? sectionFromUrl : "books-section";
    switchSection(firstSection);
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
        // Keep a stable order after create/update/delete.
        // Otherwise backend may return rows in arbitrary order and edited items can "jump" to the bottom.
        if (Array.isArray(allData)) {
            allData.sort((a, b) => {
                const aId = Number(a?.id);
                const bId = Number(b?.id);
                if (Number.isFinite(aId) && Number.isFinite(bId)) {
                    return aId - bId;
                }
                return 0;
            });
        }
        filteredData = allData;
        tableBody.innerHTML = "";
        if (!allData || allData.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading not-found">No data found</td></tr>';
            return;
        }
        applyFiltersAndRender();
    }
    catch(error){
        console.error("Failed to load data:", error);
        tableBody.innerHTML = '<tr><td colspan="100%" class="table-loading error"">Error loading data</td></tr>';
    }
    };

function renderNextChunk() {
    const tableBody = document.querySelector(".table-body");
    const loadMoreBtn = document.querySelector(".load-more");
    const nextChunk = PaginationHelper.getNextChunk(filteredData, displayedCount, PAGE_SIZE);
    nextChunk.forEach(item => {
        tableBody.insertAdjacentHTML("beforeend", currentRenderer(item));
    });
    displayedCount += nextChunk.length;
    loadMoreBtn.style.display = PaginationHelper.hasMore(filteredData, displayedCount) ? "block" : "none";
}