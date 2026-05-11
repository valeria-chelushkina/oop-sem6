export function fillUserInfo(user) {
	const profilePage = document.getElementById('profile-personal-page');
        if (profilePage) {
            profilePage.href = `/profile/${user.id}`;
        }

        const container = document.getElementById('profile-data-card');
        if (container) {
            document.getElementById('user-first-name').value = user.firstName;
            document.getElementById('user-last-name').value = user.lastName;
            document.getElementById('user-email').value = user.email;
            document.getElementById('user-id').value = user.id;
        }
};