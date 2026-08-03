import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'

export interface UserAdminItem {
  id: number
  username: string
  displayName: string
  email: string | null
  status: string
  roleCodes: string[]
  groupCodes: string[]
  teamCodes: string[]
  effectivePermissionCodes: string[]
  effectiveCaseTypeCodes: string[]
  effectiveInitiatingTeamCodes: string[]
  effectiveReceivingTeamCodes: string[]
  createdAt: string | null
  updatedAt: string | null
}

export interface GroupAdminItem {
  id: number
  code: string
  name: string
  description: string | null
  active: boolean
  roleCodes: string[]
  usernames: string[]
}

export interface RoleAdminItem {
  id: number
  code: string
  name: string
  description: string | null
  active: boolean
  permissionCodes: string[]
  caseTypeCodes: string[]
  initiatingTeamCodes: string[]
  receivingTeamCodes: string[]
}

export interface PermissionItem {
  id: number
  code: string
  name: string
  description: string | null
}

export interface CaseTypeAdminItem {
  id: number
  code: string
  name: string
  moduleKey: string
  active: boolean
  initiatingTeamCodes: string[]
  receivingTeamCodes: string[]
}

export interface TeamAdminItem {
  id: number
  code: string
  name: string
  teamType: string
  active: boolean
}

async function getList<T>(url: string): Promise<T[]> {
  const { data } = await apiClient.get<ApiSuccess<T[]>>(url)
  return data.data ?? []
}

export const adminApi = {
  listUsers: () => getList<UserAdminItem>('/api/v1/admin/users'),
  createUser: async (body: unknown) => {
    const { data } = await apiClient.post<ApiSuccess<UserAdminItem>>('/api/v1/admin/users', body)
    return data.data
  },
  updateUser: async (id: number, body: unknown) => {
    const { data } = await apiClient.put<ApiSuccess<UserAdminItem>>(`/api/v1/admin/users/${id}`, body)
    return data.data
  },
  setUserStatus: async (id: number, status: string) => {
    const { data } = await apiClient.put<ApiSuccess<UserAdminItem>>(`/api/v1/admin/users/${id}/status`, {
      status,
    })
    return data.data
  },
  resetPassword: (id: number, newPassword: string) =>
    apiClient.post(`/api/v1/admin/users/${id}/reset-password`, { newPassword }),

  listGroups: () => getList<GroupAdminItem>('/api/v1/admin/groups'),
  createGroup: async (body: unknown) => {
    const { data } = await apiClient.post<ApiSuccess<GroupAdminItem>>('/api/v1/admin/groups', body)
    return data.data
  },
  updateGroup: async (id: number, body: unknown) => {
    const { data } = await apiClient.put<ApiSuccess<GroupAdminItem>>(`/api/v1/admin/groups/${id}`, body)
    return data.data
  },
  deleteGroup: (id: number) => apiClient.delete(`/api/v1/admin/groups/${id}`),

  listRoles: () => getList<RoleAdminItem>('/api/v1/admin/roles'),
  createRole: async (body: unknown) => {
    const { data } = await apiClient.post<ApiSuccess<RoleAdminItem>>('/api/v1/admin/roles', body)
    return data.data
  },
  updateRole: async (id: number, body: unknown) => {
    const { data } = await apiClient.put<ApiSuccess<RoleAdminItem>>(`/api/v1/admin/roles/${id}`, body)
    return data.data
  },
  deleteRole: (id: number) => apiClient.delete(`/api/v1/admin/roles/${id}`),

  listPermissions: () => getList<PermissionItem>('/api/v1/admin/permissions'),

  listCaseTypes: () => getList<CaseTypeAdminItem>('/api/v1/admin/case-types'),
  createCaseType: async (body: unknown) => {
    const { data } = await apiClient.post<ApiSuccess<CaseTypeAdminItem>>('/api/v1/admin/case-types', body)
    return data.data
  },
  updateCaseType: async (id: number, body: unknown) => {
    const { data } = await apiClient.put<ApiSuccess<CaseTypeAdminItem>>(
      `/api/v1/admin/case-types/${id}`,
      body,
    )
    return data.data
  },

  listTeams: () => getList<TeamAdminItem>('/api/v1/admin/teams'),
  createTeam: async (body: unknown) => {
    const { data } = await apiClient.post<ApiSuccess<TeamAdminItem>>('/api/v1/admin/teams', body)
    return data.data
  },
  updateTeam: async (id: number, body: unknown) => {
    const { data } = await apiClient.put<ApiSuccess<TeamAdminItem>>(`/api/v1/admin/teams/${id}`, body)
    return data.data
  },
}
