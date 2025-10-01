import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/buttons/Button';

export default function SignIn() {
	const [form, setForm] = useState({ usernameOrEmail: '', password: '' });
	const [error, setError] = useState<string | null>(null);
	const navigate = useNavigate();

	const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		setForm({ ...form, [e.target.name]: e.target.value });
	};

	// handle login form submision
	const submit = async (e: React.FormEvent) => {
		e.preventDefault();
		setError(null);
		try {
			const res = await fetch('http://localhost:8080/api/users/login', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(form),
			});
			if (!res.ok) {
				const txt = await res.text();
				setError(txt || 'Login failed');
				return;
			}
			const data = await res.json();
			localStorage.setItem('token', data.token);
			navigate('/home');
		} catch (err: any) {
			setError(err.message || 'Network error');
		}
	};

	return (
		<div className="max-w-md mx-auto">
			<h1 className="text-2xl font-bold mb-4">Sign In</h1>
			<form onSubmit={submit} className="space-y-3">
				<div>
					<label className="block text-sm font-medium">Username or Email</label>
					<input name="usernameOrEmail" value={form.usernameOrEmail} onChange={onChange}
						className="mt-1 w-full rounded-md border px-3 py-2" />
				</div>
				<div>
					<label className="block text-sm font-medium">Password</label>
					<input name="password" type="password" value={form.password} onChange={onChange}
						className="mt-1 w-full rounded-md border px-3 py-2" />
				</div>
				{error && <div className="text-red-600">{error}</div>}
				<Button type="submit">Sign In</Button>
			</form>
		</div>
	);
}


