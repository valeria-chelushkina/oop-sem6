export const BookItemApi = {
	async getByBookId(bookId) {
        const response = await fetch(`/api/book-items?bookId=${bookId}`);
        if (!response.ok) throw new Error(`Failed to fetch book items by book id: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`/api/book-items?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch book items by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
        },
    async getAll() {
        const response = await fetch(`/api/book-items`);
        if (!response.ok) throw new Error(`Failed to fetch book items: ${response.status}`);
        const data = await response.json();
        return data;
    }
};