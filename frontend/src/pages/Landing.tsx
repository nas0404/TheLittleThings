import { Link } from 'react-router-dom';
import Button from '../components/buttons/Button';

export default function Landing() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">Welcome to TheLittleThings</h1>
        <p className="text-lg text-gray-600">Track your daily achievements and celebrate small wins</p>
      </div>
      
      <div className="space-x-4">
        <Link to="/register">
          <Button>Register</Button>
        </Link>
        <Link to="/login">
          <Button className="bg-white text-blue-600 border border-blue-600 hover:bg-gray-50 hover:bg-opacity-50">
            Sign In
          </Button>
        </Link>
      </div>
    </div>
  );
}
