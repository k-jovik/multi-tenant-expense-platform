import { Badge } from "@/components/ui/badge";
import type { ExpenseStatus } from "@/types/api";

export function StatusBadge({ status }: { status: ExpenseStatus }) {
  const variant =
    status === "APPROVED"
      ? "default"
      : status === "REJECTED"
        ? "destructive"
        : "secondary";
    return (
        <Badge variant={variant}>{status}</Badge>
    )
}
