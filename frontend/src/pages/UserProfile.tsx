import { useEffect, useState } from 'react';
import Button from '../components/buttons/Button';
import { UserAPI } from '../api/users';
import { ApiError } from '../api/http';
import { useNavigate } from 'react-router-dom';

export default function UserProfile() {
	const [user, setUser] = useState<any>(null);
	const [error, setError] = useState<string | null>(null);
	const [oldPass, setOldPass] = useState('');
	const [newPass, setNewPass] = useState('');
	const navigate = useNavigate();
	// get user info when component loads
	useEffect(() => {
		const fetchUserData = async () => {
			try {
				const data = await UserAPI.me();
				console.log('User data received:', data);
				setUser(data);
			} catch (err) {
				if (err instanceof ApiError && err.status === 401) {
					setError('Please log in');
					localStorage.removeItem('token');
				} else {
					setError('Unable to fetch user data');
				}
			}
		};
		
		const token = localStorage.getItem('token');
		if (token) {
			fetchUserData();
		}
	}, []);

	// password change function
	const change = async () => {
		const token = localStorage.getItem('token');
		try {
			const res = await fetch('/api/users/change-password', {
				method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
				body: JSON.stringify({ oldPassword: oldPass, newPassword: newPass })
			});
			if (!res.ok) {
				setError(await res.text());
				return;
			}
			setError('Password changed');
		} catch (e) {
			setError('Network');
		}
	};

	const [newUsername, setNewUsername] = useState('');
	const changeUsername = async () => {
		const token = localStorage.getItem('token');
		try {
			const res = await fetch('/api/users/change-username', {
				method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
				body: JSON.stringify({ newUsername })
			});
			if (!res.ok) {
				setError(await res.text());
				return;
			}
			const json = await res.json();
			if (json.token) {
				localStorage.setItem('token', json.token);
				setUser((u: any) => ({ ...u, username: json.username }));
				setError('Username changed');
			} else {
				setError('No token returned');
			}
		} catch (e) {
			setError('Network');
		}
	};

	// logout and clear token
	const logout = async () => {
		UserAPI.logout();
		window.location.href = '/';
	};

	return (

	<div className="max-w-lg mx-auto">
		{/* Top bar with Settings button */}
		<div className="mb-6 flex items-center justify-between">
			<h1 className="text-3xl font-bold text-gray-900">User Profile</h1>
			<Button onClick={() => navigate('/settings')}>Settings</Button>
		</div>


		<div className="max-w-lg mx-auto">
			<h1 className="text-3xl font-bold text-gray-900 mb-6">User Profile</h1>
			{error && (
				<div className="mb-4 rounded-md bg-red-50 border border-red-200 p-3">
					<p className="text-sm text-red-700">{error}</p>
				</div>
			)}
			{user ? (
				<div className="space-y-4">
					<div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
						<div className="space-y-3">
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Username:</span>
								<span className="text-gray-900">{user.username}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Email:</span>
								<span className="text-gray-900">{user.email}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Age:</span>
								<span className="text-gray-900">{user.age !== null && user.age !== undefined && user.age !== '' ? user.age : 'Not specified'}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Date of Birth:</span>
								<span className="text-gray-900">{user.dob ? new Date(user.dob).toLocaleDateString() : 'Not specified'}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Gender:</span>
								<span className="text-gray-900">{user.gender || 'Not specified'}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Region:</span>
								<span className="text-gray-900">{user.region || 'Not specified'}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Streaks:</span>
								<span className="text-gray-900">{user.streaks !== null && user.streaks !== undefined ? user.streaks : 0}</span>
							</div>
							<div className="flex justify-between">
								<span className="font-medium text-gray-700">Last login:</span>
								<span className="text-gray-900">{user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never'}</span>
							</div>
						</div>
					</div>

					<div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
						<h3 className="text-lg font-semibold mb-4">Change Username</h3>
						<input 
							value={newUsername} 
							onChange={(e) => setNewUsername(e.target.value)} 
							placeholder="New username" 
							className="w-full mb-3 rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" 
						/>
						<Button onClick={changeUsername}>Change Username</Button>
					</div>

					<div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
						<h3 className="text-lg font-semibold mb-4">Change Password</h3>
						<input 
							type="password" 
							placeholder="Current password" 
							value={oldPass} 
							onChange={(e) => setOldPass(e.target.value)} 
							className="w-full mb-3 rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" 
						/>
						<input 
							type="password" 
							placeholder="New password" 
							value={newPass} 
							onChange={(e) => setNewPass(e.target.value)} 
							className="w-full mb-3 rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" 
						/>
						<Button onClick={change}>Change Password</Button>
					</div>

					<div>
						<Button onClick={logout} className="bg-red-600">Logout</Button>
						<div className="mt-3">
							<Button onClick={async () => {
								if (!confirm('Delete your account? This is irreversible.')) return;
								const token = localStorage.getItem('token');
								try {
									const res = await fetch('/api/users/', { method: 'DELETE', headers: { Authorization: `Bearer ${token}` } });
									if (!res.ok) { setError('Delete failed'); return; }
									localStorage.removeItem('token');
									window.location.href = '/';
								} catch (e) { setError('Network'); }
							}} className="bg-red-700">Delete account</Button>
						</div>
					</div>
				</div>
			) : (
				<div>Not logged in</div>
			)}
		</div>
	</div>
	);
}
