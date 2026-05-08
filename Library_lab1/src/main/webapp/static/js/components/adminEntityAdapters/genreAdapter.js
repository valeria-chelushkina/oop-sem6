export function getGenrePayloadFromForm(formEl) {
  const input = formEl?.querySelector("#genre-name");
  const name = input?.value?.trim() ?? "";
  return { name };
}

export function fillGenreForm(formEl, genre) {
  const input = formEl?.querySelector("#genre-name");
  if (input) {
    input.value = genre?.name ?? "";
  }
}

