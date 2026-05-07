export function initializeSelects() {
  setupSelect2("#author-select", {
    placeholder: "Choose authors",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#genre-select", {
    placeholder: "Choose genres",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#bookItem-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#loan-type", {
    placeholder: "Select a type",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#loan-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
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