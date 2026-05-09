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

  setupSelect2("#book-item-status", {
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

/**
 * Додає новий елемент у Select2 і вибирає його
 */
export function appendAndSelect(selector, newItem, textProp) {
    const $select = $(selector);
    if (!$select.length) return;

    // ДЕБАГ: Перевірте в консолі, чи приходить сюди текст
    console.log("Adding to select:", newItem, "Text property:", textProp, "Value:", newItem[textProp]);

    // Якщо раптом newItem[textProp] порожній, ставимо запасне значення
    const text = newItem[textProp] || `New Item (ID: ${newItem.id})`;
    const id = newItem.id;

    // Створюємо опцію: new Option(text, id, defaultSelected, selected)
    // Важливо: ставимо false, false, а вибір робимо через .val()
    const newOption = new Option(text, id, false, false);

    // Додаємо в DOM
    $select.append(newOption);

    // Отримуємо поточний масив значень (для multiple select)
    let currentValues = $select.val() || [];
    if (!Array.isArray(currentValues)) currentValues = [currentValues];

    // Додаємо новий ID, якщо його ще немає
    const idStr = String(id);
    if (!currentValues.includes(idStr)) {
        currentValues.push(idStr);
    }

    // Оновлюємо значення та примусово синхронізуємо Select2
    $select.val(currentValues).trigger('change');
}