import { useQuery } from '@tanstack/react-query';
import api from '@/lib/api';
import type { Account } from '@/types/api';

export function useAccounts() {
  return useQuery({
    queryKey: ['accounts'],
    queryFn: () => api.get<Account[]>('/api/accounts').then(r => r.data),
  });
}