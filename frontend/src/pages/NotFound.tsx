import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="space-y-3">
      <h1 className="text-2xl font-bold">Page not found</h1>
      <Link to="/" className="text-blue-600 underline">Back to home</Link>
    </div>
  );
}
