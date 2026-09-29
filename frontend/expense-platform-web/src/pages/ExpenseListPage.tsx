import { useState } from "react";
import { Link } from "react-router-dom";
import { useExpenses } from "@/hooks/useExpenses";
import { StatusBadge } from "@/components/StatusBadge";
import { formatCurrency } from "@/lib/format";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { ExpenseStatus } from "@/types/api";

const FILTER_OPTIONS = [
  { label: "All", value: "ALL" },
  { label: "Submitted", value: "SUBMITTED" },
  { label: "Approved", value: "APPROVED" },
  { label: "Rejected", value: "REJECTED" },
] as const;

type Filter = ExpenseStatus | "ALL";

export default function ExpenseListPage() {
  const { data: expenses, isLoading } = useExpenses();
  const [statusFilter, setStatusFilter] = useState<Filter>("ALL");

  if (isLoading) {
    return <div className="text-muted-foreground">Loading...</div>;
  }

  const filtered =
    expenses?.filter(
      (e) => statusFilter === "ALL" || e.status === statusFilter,
    ) ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Expenses</h1>
        <Link to="/expenses/add">
          <Button>+ Add Expense</Button>
        </Link>
      </div>

      <div className="flex items-center gap-2">
        <span className="text-sm text-muted-foreground">Status:</span>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as Filter)}
          className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm"
        >
          {FILTER_OPTIONS.map((f) => (
            <option key={f.value} value={f.value}>
              {f.label}
            </option>
          ))}
        </select>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Category</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Currency</TableHead>
            <TableHead>Description</TableHead>
            <TableHead className="text-right">Amount</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {filtered.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={5}
                className="text-center text-muted-foreground"
              >
                No expenses found.
              </TableCell>
            </TableRow>
          ) : (
            filtered.map((expense) => (
              <TableRow key={expense.id}>
                <TableCell className="font-medium">
                  {expense.category}
                </TableCell>
                <TableCell>
                  <StatusBadge status={expense.status} />
                </TableCell>
                <TableCell>{expense.currency}</TableCell>
                <TableCell>{expense.description}</TableCell>
                <TableCell className="text-right font-mono">
                  {formatCurrency(expense.amountMinor)}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
        {filtered.length > 0 && (
          <TableFooter>
            <TableRow>
              <TableCell colSpan={4}>Total</TableCell>
              <TableCell className="text-right font-mono">
                {formatCurrency(
                  filtered.reduce((sum, e) => sum + e.amountMinor, 0),
                )}
              </TableCell>
            </TableRow>
          </TableFooter>
        )}
      </Table>
    </div>
  );
}
