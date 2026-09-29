import { useAuth } from "@/contexts/AuthContext";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";

export default function NavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="border-b bg-background">
      <div className="flex gap-4 h-14 items-center px-6">
        <Link to="/" className="font-bold">
          Expense Platform
        </Link>
        <Link
          to="/"
          className="text-sm text-muted-foreground hover:text-foreground"
        >
          Dashboard
        </Link>
        <Link
          to="/expenses"
          className="text-sm text-muted-foreground hover:text-foreground"
        >
          Expenses
        </Link>
        <div className="ml-auto flex items-center gap-4">
          <span className="text-sm text-muted-foreground">{user?.role}</span>
          <Button variant="outline" size="sm" onClick={handleLogout}>
            Logout
          </Button>
        </div>
      </div>
    </nav>
  );
}
