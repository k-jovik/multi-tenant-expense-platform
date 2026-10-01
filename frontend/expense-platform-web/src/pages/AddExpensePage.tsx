import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import type { Expense } from "@/types/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import axios from "axios";

interface ExpenseFormData {
  amount: string;
  currency: string;
  category: string;
  description?: string;
}

const CURRENCIES = ["USD", "EUR", "GBP", "MKD"];
const CATEGORIES = ["Meals", "Travel", "Software", "Equipment"];

export default function AddExpensePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string>("");

  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<ExpenseFormData>({
    defaultValues: {
      currency: "USD",
      category: "Meals",
    },
  });

  function parseAmountToMinor(input: string): number | null {
    const trimmed = input.trim();
    // allow "5", "5.5", "5.50", "5.123"
    if (!/^\d+(\.\d{1,})?$/.test(trimmed)) return null;

    const [whole, frac = ""] = trimmed.split(".");
    const fracPadded = (frac + "00").slice(0, 2); // truncate to 2 digits
    return Number(whole) * 100 + Number(fracPadded);
  }

  const onSubmit = async (data: ExpenseFormData) => {
    setError("");
    const amountMinor = parseAmountToMinor(data.amount);

    if (amountMinor === null || amountMinor <= 0) {
      setError("Amount must be a positive number.");
      return;
    }

    try {
      await api.post<Expense>("/api/expenses", {
        amountMinor,
        currency: data.currency,
        category: data.category,
        description: data.description,
      });

      await queryClient.invalidateQueries({ queryKey: ["expenses"] });

      navigate("/expenses");
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError("Failed to submit expense. Please try again.");
      }
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-background">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Add Expense</CardTitle>
          <CardDescription>Submit a new expense claim</CardDescription>
        </CardHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="amount">Amount</Label>
              <Input
                id="amount"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="5.00"
                {...register("amount", { required: true })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="currency">Currency</Label>
              <select
                id="currency"
                {...register("currency", { required: true })}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                {CURRENCIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="category">Category</Label>
              <select
                id="category"
                {...register("category", { required: true })}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                {CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Input
                id="description"
                placeholder="What was this for?"
                {...register("description")}
              />
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
          </CardContent>

          <CardFooter className="flex flex-col gap-3">
            <Button type="submit" className="w-full" disabled={isSubmitting}>
              {isSubmitting ? "Submitting..." : "Submit"}
            </Button>
            <Link
              to="/expenses"
              className="text-sm text-muted-foreground underline"
            >
              Cancel
            </Link>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
