export const BookItemApi = {
	async getByBookId(bookId) {
		const url = '/api/book-items?bookId='+bookId;
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch books');
        const data = await response.json();
        return data;
    },
    async getById(id) {
            const response = await fetch(`/api/book-items?id=${id}`);
            const data = await response.json();
            return Array.isArray(data) ? data[0] : data;
        }
};