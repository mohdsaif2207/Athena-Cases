import { useMutation } from '@tanstack/react-query'
import { createExrtCase } from '../api/exrtApi'
import type { ExrtCaseCreatePayload } from '../types/exrt.types'

export function useCreateExrtCase() {
  return useMutation({
    mutationFn: (payload: ExrtCaseCreatePayload) => createExrtCase(payload),
  })
}
