/**
 * Generic CRUD helpers for Admin Panel modals.
 * These functions are entity-agnostic; pass adapters and api functions as arguments.
 */

export async function openEditModal({
  id,
  apiGetById,
  overlayEl,
  titleEl,
  titleText = "Update",
  formEl,
  renderFormHtml,
  fillForm,
  setModalContext,
  modalContext,
}) {
  const entity = await apiGetById(id);
  if (!entity) {
    throw new Error("Entity not found");
  }

  if (titleEl) {
    titleEl.textContent = titleText;
  }
  if (formEl) {
    formEl.innerHTML = renderFormHtml();
    if (typeof fillForm === "function") {
      fillForm(formEl, entity);
    }
  }
  if (typeof setModalContext === "function" && modalContext) {
    setModalContext({ ...modalContext, mode: "update", id: entity.id });
  }
  overlayEl?.classList.add("active");
}

export async function saveEntity({
  mode,
  id,
  formEl,
  getPayloadFromForm,
  validate,
  apiCreate,
  apiUpdate,
}) {
  const payload = getPayloadFromForm(formEl);

  if (typeof validate === "function") {
    validate(payload);
  }

  if (mode === "update") {
    // Force the ID into the payload to ensure it's not overwritten by an 'undefined' key
    const updateData = { ...payload, id: Number(id) };
    return await apiUpdate(updateData);
  }

  return await apiCreate(payload);
}

export async function deleteEntity({ id, apiDelete }) {
  return await apiDelete(id);
}

