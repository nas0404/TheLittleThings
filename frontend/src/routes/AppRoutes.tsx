// import { Routes, Route } from 'react-router-dom';
import { Routes, Route } from "react-router-dom";
import Home from '../pages/Home';
import About from '../pages/About';
import Settings from '../pages/Settings';
import NotFound from '../pages/NotFound';
import Landing from '../pages/Landing';
import Register from '../pages/Register';
import SignIn from '../pages/SignIn';
import UserProfile from '../pages/UserProfile';
import CategoryPage from '../pages/Categories.tsx';
import Leaderboard from '../pages/Leaderboard.tsx';
import Wins from '../pages/Wins.tsx';
import Goals from '../pages/Goals.tsx';
import Journal from '../pages/Journal';
import FriendsPage from "../pages/FriendsPage";
import CreateGoalsPage from "../pages/CreateGoals";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/register" element={<Register />} />
      <Route path="/login" element={<SignIn />} />
      <Route path="/user" element={<UserProfile />} />
      <Route path="/home" element={<Home />} />
      <Route path="/about" element={<About />} />
      <Route path="/settings" element={<Settings />} />
      <Route path="/categories" element={<CategoryPage />} />
      <Route path="/goals" element={<Goals />} />
      <Route path="/wins" element={<Wins />} />
      <Route path="/journal" element={<Journal />} />
      <Route path="/leaderboard" element={<Leaderboard />} />
      <Route path="*" element={<NotFound />} />
      <Route path="/friends" element={<FriendsPage />} />
      <Route path="/create-goals" element={<CreateGoalsPage />} />
    </Routes>
  );
}
