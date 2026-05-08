export function initializeSelects() {
  setupSelect2("#author-select", {
    placeholder: "Choose authors",
    allowClear: true,
    width: "100%",
    dropdownParent: $("#modal-overlay"),

  });

  setupSelect2("#genre-select", {
    placeholder: "Choose genres",
    allowClear: true,
    width: "100%",
    dropdownParent: $("#modal-overlay"),
  });

  setupSelect2("#bookItem-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $("#modal-overlay"),
  });

  setupSelect2("#loan-type", {
    placeholder: "Select a type",
    allowClear: true,
    width: "100%",
    dropdownParent: $("#modal-overlay"),
  });

  setupSelect2("#loan-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $("#modal-overlay"),
  });
}

function setupSelect2(selector, options) {
  const element = $(selector);

  if (!element.length) return;

  if (element.hasClass("select2-hidden-accessible")) {
    element.select2("destroy");
  }

  element.select2(options);
}

// function for filling select with API data
export async function fillSelect(selector, apiCall, valueProp, textProp) {
    const select = document.querySelector(selector);
    if (!select) return;

    try {
        const data = await apiCall();
        select.innerHTML = data.map(item =>
            `<option value="${item[valueProp]}">${item[textProp]}</option>`
        ).join('');
        if ($(select).data('select2')) {
            $(select).trigger('change');
        }
    } catch (e) {
        console.error("Select fill error " + selector, e);
    }
}