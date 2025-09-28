import { NavLink } from 'react-router-dom';

type Props = { children: React.ReactNode };

export default function RootLayout({ children }: Props) {
  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <header className="border-b bg-white">
        <nav className="mx-auto flex max-w-6xl items-center gap-4 p-4">
          <NavLink to="/" className="font-semibold">TheLittleThing</NavLink>
          <div className="ml-auto flex items-center gap-3">
            <NavLink to="/home" className="hover:underline">Home</NavLink>
            <NavLink to="/about" className="hover:underline">About</NavLink>
            <NavLink to="/settings" className="hover:underline">Settings</NavLink>
            <NavLink to="/categories" className="hover:underline">Categories</NavLink>
            <NavLink to="/goals" className="hover:underline">Goals</NavLink>
            <NavLink to="/wins" className="hover:underline">Wins</NavLink>
            <NavLink to="/journal" className="hover:underline">Journal</NavLink>
            <NavLink to="/leaderboard" className="hover:underline">Leaderboard</NavLink>
            <NavLink to="/categories/:categoryId" className="hover:underline">CategoryGoalsPage</NavLink>
              <NavLink to="/user" className="hover:underline">Account</NavLink>


          </div>
        </nav>
      </header>

      <main className="mx-auto max-w-6xl p-6">{children}</main>

      <footer className="mx-auto max-w-6xl p-6 text-sm text-gray-500">
        © {new Date().getFullYear()} TheLittleThing
      </footer>
    </div>
  );
}
