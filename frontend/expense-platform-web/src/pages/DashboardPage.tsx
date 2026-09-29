import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import type { Expense } from "@/types/api";
import { useAccounts } from "@/hooks/useAccounts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { formatCurrency } from "@/lib/format";
import { useExpenses } from "@/hooks/useExpenses";

export default function DashboardPage() {
  const { data: accounts, isLoading: accountsLoading } = useAccounts();

  const { data: pending } = useExpenses();

  const { data: expenses } = useExpenses();

  if (accountsLoading) {
    return <div className="text-muted-foreground">Loading...</div>;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Dashboard</h1>

      {/* Balance cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {accounts?.map((account) => (
          <Card key={account.id}>
            <CardHeader>
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {account.type === "PAYABLE" ? "Payable" : account.name}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">
                {formatCurrency(account.balanceMinor)}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Pending count */}
      <Card>
        <CardContent className="pt-6">
          <p className="text-sm text-muted-foreground">Pending approvals</p>
          <p className="text-3xl font-bold">{pending?.length ?? 0}</p>
        </CardContent>
      </Card>

      {/* Recent expenses */}
      <div>
        <h2 className="text-lg font-semibold mb-3">Recent expenses</h2>
        {expenses && expenses.length > 0 ? (
          <div className="border rounded-md divide-y">
            {expenses.slice(0, 5).map((expense) => (
              <div
                key={expense.id}
                className="flex items-center justify-between p-3"
              >
                <div>
                  <p className="font-medium">{expense.category}</p>
                  <p className="text-sm text-muted-foreground">
                    {expense.description}
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <span className="font-mono">
                    {formatCurrency(expense.amountMinor)}
                  </span>
                  <Badge>{expense.status}</Badge>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-muted-foreground">No expenses yet.</p>
        )}
      </div>
    </div>
  );
}
