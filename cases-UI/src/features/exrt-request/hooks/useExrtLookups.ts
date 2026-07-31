import { useQueries } from '@tanstack/react-query'
import {
  fetchAssignees,
  fetchCarriers,
  fetchContacts,
  fetchExrtEnumLookup,
  fetchReasonCodes,
  fetchTierIiAgents,
} from '../api/exrtApi'

export function useExrtLookups(clientId?: string) {
  const results = useQueries({
    queries: [
      { queryKey: ['exrt', 'clients'], queryFn: () => fetchExrtEnumLookup('clients') },
      { queryKey: ['exrt', 'products'], queryFn: () => fetchExrtEnumLookup('products') },
      { queryKey: ['exrt', 'carriers'], queryFn: fetchCarriers },
      { queryKey: ['exrt', 'tier-ii'], queryFn: fetchTierIiAgents },
      { queryKey: ['exrt', 'assignees'], queryFn: fetchAssignees },
      { queryKey: ['exrt', 'reason-codes'], queryFn: fetchReasonCodes },
      { queryKey: ['exrt', 'contacts', clientId], queryFn: () => fetchContacts(clientId), enabled: true },
      { queryKey: ['exrt', 'types'], queryFn: () => fetchExrtEnumLookup('types') },
      { queryKey: ['exrt', 'dispositions'], queryFn: () => fetchExrtEnumLookup('dispositions') },
      { queryKey: ['exrt', 'inquiry-sources'], queryFn: () => fetchExrtEnumLookup('inquiry-sources') },
      { queryKey: ['exrt', 'action-needed'], queryFn: () => fetchExrtEnumLookup('action-needed') },
      { queryKey: ['exrt', 'escalation-reasons'], queryFn: () => fetchExrtEnumLookup('escalation-reasons') },
      { queryKey: ['exrt', 'case-origins'], queryFn: () => fetchExrtEnumLookup('case-origins') },
      { queryKey: ['exrt', 'statuses'], queryFn: () => fetchExrtEnumLookup('statuses') },
      { queryKey: ['exrt', 'priorities'], queryFn: () => fetchExrtEnumLookup('priorities') },
    ],
  })

  const [
    clients,
    products,
    carriers,
    tierIiAgents,
    assignees,
    reasonCodes,
    contacts,
    types,
    dispositions,
    inquirySources,
    actionNeeded,
    escalationReasons,
    caseOrigins,
    statuses,
    priorities,
  ] = results

  return {
    isLoading: results.some((r) => r.isLoading),
    isError: results.some((r) => r.isError),
    clients: clients.data ?? [],
    products: products.data ?? [],
    carriers: carriers.data ?? [],
    tierIiAgents: tierIiAgents.data ?? [],
    assignees: assignees.data ?? [],
    reasonCodes: reasonCodes.data ?? [],
    contacts: contacts.data ?? [],
    types: types.data ?? [],
    dispositions: dispositions.data ?? [],
    inquirySources: inquirySources.data ?? [],
    actionNeeded: actionNeeded.data ?? [],
    escalationReasons: escalationReasons.data ?? [],
    caseOrigins: caseOrigins.data ?? [],
    statuses: statuses.data ?? [],
    priorities: priorities.data ?? [],
  }
}
