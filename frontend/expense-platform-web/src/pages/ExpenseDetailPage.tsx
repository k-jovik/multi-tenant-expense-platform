import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Check, X } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useExpenses } from "@/hooks/useExpenses";
import type { Expense } from "@/types/api";
import { StatusBadge } from "@/components/StatusBadge";
import { formatCurrency } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import api, { getApiErrorMessage } from "@/lib/api";

export default function ExpenseDetailPage() {
  const { user } = useAuth();
  const { id } = useParams<{ id: string }>();
  const { data: expenses, isLoading } = useExpenses();
  const queryClient = useQueryClient();

  const [error, setError] = useState("");
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState("");

  const hasAccess = user?.role === "ADMIN" || user?.role === "MANAGER";

  const approveMutation = useMutation({
    mutationFn: () =>
      api.patch<Expense>(`/api/expenses/${id}/approve`).then((res) => res.data),
    onMutate: () => setError(""),
    onSuccess: (updated) => {
      queryClient.setQueryData<Expense[]>(["expenses"], (old) =>
        old?.map((e) => (e.id === updated.id ? updated : e)),
      );
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
    },
    onError: (err) =>
      setError(getApiErrorMessage(err, "Failed to approve. Please try again.")),
  });

  const rejectMutation = useMutation({
    mutationFn: (reason: string) =>
      api
        .patch<Expense>(`/api/expenses/${id}/reject`, { reason })
        .then((res) => res.data),
    onMutate: () => setError(""),
    onSuccess: (updated) => {
      queryClient.setQueryData<Expense[]>(["expenses"], (old) =>
        old?.map((e) => (e.id === updated.id ? updated : e)),
      );
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      setRejectDialogOpen(false);
      setRejectReason("");
    },
    onError: (err) =>
      setError(getApiErrorMessage(err, "Failed to reject. Please try again.")),
  });

  if (isLoading) {
    return <div className="text-muted-foreground">Loading...</div>;
  }

  const expense = expenses?.find((e) => e.id === id);

  if (!expense) {
    return (
      <div className="space-y-4">
        <Link
          to="/expenses"
          className="text-sm text-muted-foreground hover:underline"
        >
          ← Back to expenses
        </Link>
        <p className="text-muted-foreground">Expense not found.</p>
      </div>
    );
  }

  return (
    <div className="flex justify-center">
      <div className="space-y-4 max-w-2xl w-full">
        <Link
          to="/expenses"
          className="text-sm text-muted-foreground hover:underline"
        >
          ← Back to expenses
        </Link>

        <Card>
          <CardHeader>
            <CardTitle>Expense Details</CardTitle>
          </CardHeader>

          <CardContent className="space-y-6">
            <div className="grid grid-cols-[140px_1fr] gap-x-4 gap-y-3">
              <span className="text-sm text-muted-foreground">Amount</span>
              <span className="font-mono font-medium">
                {formatCurrency(expense.amountMinor, expense.currency)}
              </span>

              <span className="text-sm text-muted-foreground">Category</span>
              <span>{expense.category}</span>

              <span className="text-sm text-muted-foreground">Description</span>
              <span>{expense.description ?? "—"}</span>

              <span className="text-sm text-muted-foreground">Status</span>
              <span>
                <StatusBadge status={expense.status} />
              </span>

              {expense.rejectionReason && (
                <>
                  <span className="text-sm text-muted-foreground">
                    Rejection reason
                  </span>
                  <span className="text-destructive">
                    {expense.rejectionReason}
                  </span>
                </>
              )}
            </div>

            {hasAccess && expense.status === "SUBMITTED" && (
              <div className="flex gap-3 pt-4 border-t">
                <Button
                  onClick={() => approveMutation.mutate()}
                  disabled={approveMutation.isPending}
                >
                  <Check className="mr-2 h-4 w-4" />
                  {approveMutation.isPending ? "Approving..." : "Approve"}
                </Button>

                <Button
                  variant="destructive"
                  onClick={() => setRejectDialogOpen(true)}
                >
                  <X className="mr-2 h-4 w-4" />
                  Reject
                </Button>

                <Dialog
                  open={rejectDialogOpen}
                  onOpenChange={(open) => {
                    setRejectDialogOpen(open);
                    if (!open) setRejectReason("");
                  }}
                >
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Reject Expense</DialogTitle>
                      <DialogDescription>
                        Provide a reason. The employee will see this.
                      </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-2 py-4">
                      <Label htmlFor="reason">Reason</Label>
                      <Input
                        id="reason"
                        value={rejectReason}
                        onChange={(e) => setRejectReason(e.target.value)}
                        placeholder="e.g. Receipt missing"
                      />
                    </div>

                    <DialogFooter>
                      <Button
                        variant="outline"
                        onClick={() => setRejectDialogOpen(false)}
                      >
                        Cancel
                      </Button>
                      <Button
                        variant="destructive"
                        onClick={() => rejectMutation.mutate(rejectReason)}
                        disabled={
                          !rejectReason.trim() || rejectMutation.isPending
                        }
                      >
                        {rejectMutation.isPending ? "Rejecting..." : "Reject"}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            )}

            {hasAccess && expense.status === "APPROVED" && (
              <p className="text-sm text-muted-foreground pt-4 border-t">
                This expense has already been approved.
              </p>
            )}

            {hasAccess && expense.status === "REJECTED" && (
              <p className="text-sm text-muted-foreground pt-4 border-t">
                This expense was rejected.
              </p>
            )}

            {!hasAccess && expense.status === "SUBMITTED" && (
              <p className="text-sm text-muted-foreground pt-4 border-t">
                Waiting for manager approval.
              </p>
            )}

            {error && (
              <p className="text-sm text-destructive pt-4 border-t">{error}</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}