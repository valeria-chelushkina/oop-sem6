export const AuthorApi = {
	async getAll() {
        const response = await fetch(`/api/authors`);
        if (!response.ok) throw new Error(`Failed to fetch authors: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`/api/authors?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch authors by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    }
};