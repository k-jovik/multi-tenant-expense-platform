import { useQuery } from "@tanstack/react-query";
import type { Expense } from "@/types/api";
import api from "@/lib/api";

export function useExpenses() {
  return useQuery({
    queryKey: ["expenses"],
    queryFn: () => api.get<Expense[]>("api/expenses").then((r) => r.data),
  });
}
