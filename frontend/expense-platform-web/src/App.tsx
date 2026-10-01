import { Routes, Route, Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "@/contexts/AuthContext";
import LoginPage from "@/pages/LoginPage";
import RegisterPage from "@/pages/RegisterPage";
import DashboardPage from "@/pages/DashboardPage";
import Layout from "./components/Layout";
import AddExpenesePage from "@/pages/AddExpensePage"
import ExpenseListPage from "./pages/ExpenseListPage";
import ExpenseDetailPage from "./pages/ExpenseDetailPage";

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<DashboardPage />}></Route>
        <Route path="/expenses" element={<ExpenseListPage/>}></Route>
        <Route path="/expenses/add" element={<AddExpenesePage/>}></Route>
        <Route path="/expenses/:id" element={<ExpenseDetailPage/>}></Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />

    </Routes>
  );
}
