import { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import api, { getApiErrorMessage } from "@/lib/api";
import type { AuthResponse } from "@/types/api";
import { useAuth } from "@/contexts/AuthContext";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface RegisterPageContent {
  email: string;
  password: string;
  tenantName: string;
  fullName: string;
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<RegisterPageContent>();
  const [error, setError] = useState<string>("");

  const { login } = useAuth();

  const onSubmit = async (data: RegisterPageContent) => {
    setError("");
    try {
      const response = await api.post<AuthResponse>("/auth/register", data);
      login(response.data);
      navigate("/");
    } catch (err) {
      setError(
        getApiErrorMessage(err, "Registration failed. Please try again."),
      );
    }
  };

  return (
    <div className="min-h-screen flex justify-center items-center">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Register an account</CardTitle>
        </CardHeader>
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="flex flex-col gap-4">
              <div className="grid gap-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="m@example.com"
                  required
                  {...register("email", { required: true })}
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  required
                  {...register("password", { required: true })}
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="tenantName">Tenant Name</Label>
                <Input
                  id="tenantName"
                  placeholder="Acme Corp"
                  required
                  {...register("tenantName", { required: true })}
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="fullName">Full Name</Label>
                <Input
                  id="fullName"
                  required
                  {...register("fullName", { required: true })}
                />
              </div>
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
          </CardContent>

          <CardFooter className="flex flex-col gap-3 mt-4">
            <Button type="submit" disabled={isSubmitting} className="w-full">
              {isSubmitting ? "Creating account..." : "Create account"}
            </Button>
            <p className="text-sm text-muted-foreground">
              Already have an account?{" "}
              <Link to="/login" className="text-primary underline">
                Sign in
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
