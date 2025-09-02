import { Routes, Route } from 'react-router-dom';
import Home from '../pages/Home';
import About from '../pages/About';
import Settings from '../pages/Settings';
import NotFound from '../pages/NotFound';
import CategoriesAndGoals from '../pages/CategoriesAndGoals.tsx';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/about" element={<About />} />
      <Route path="/settings" element={<Settings />} />
      <Route path="/categoriesandgoals" element={<CategoriesAndGoals />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
