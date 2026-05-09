export function initializeSelects( window ) {
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

  setupSelect2("#book-item-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $(window),
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
    const $select = $(selector);
    if (!$select.length) return;
    try {
        const data = await apiCall();
        const optionsHtml = data.map(item =>
            `<option value="${item[valueProp]}">${item[textProp]}</option>`
        ).join('');

        $select.html(optionsHtml);
        if ($select.data('select2')) {
            $select.trigger('change');
        }
    } catch (e) {
        console.error("Select fill error " + selector, e);
    }
}

export function appendAndSelect(selector, newItem, textProp) {
    const $select = $(selector);
    if (!$select.length) return;
    const text = newItem[textProp] || `New Item (ID: ${newItem.id})`;
    const id = newItem.id;
    const newOption = new Option(text, id, false, false);
    $select.append(newOption);
    let currentValues = $select.val() || [];
    if (!Array.isArray(currentValues)) currentValues = [currentValues];
    const idStr = String(id);
    if (!currentValues.includes(idStr)) {
        currentValues.push(idStr);
    }
    $select.val(currentValues).trigger('change');
}