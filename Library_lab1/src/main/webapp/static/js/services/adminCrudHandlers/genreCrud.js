import { GenreApi } from "../../api/genreApi.js";
import { fillGenreForm, getGenrePayloadFromForm } from "../../components/adminEntityAdapters/genreAdapter.js";
import { deleteEntity, openEditModal, saveEntity } from "../adminCrudService.js";

export async function openGenreEditModal(id, { overlayEl, titleEl, formEl, renderFormHtml, setModalContext }) {
  return await openEditModal({
    id,
    apiGetById: GenreApi.getById,
    overlayEl,
    titleEl,
    titleText: "Update genre",
    formEl,
    renderFormHtml,
    fillForm: fillGenreForm,
    setModalContext,
    modalContext: { section: "genres-section" },
  });
}

export async function saveGenre({ mode, id, formEl }) {
  return await saveEntity({
    mode,
    id,
    formEl,
    getPayloadFromForm: getGenrePayloadFromForm,
    validate: (payload) => {
      if (!payload?.name) {
        throw new Error("Genre name is required");
      }
    },
    apiCreate: GenreApi.create,
    apiUpdate: GenreApi.update,
  });
}

export async function deleteGenre(id) {
  return await deleteEntity({ id, apiDelete: GenreApi.delete });
}

