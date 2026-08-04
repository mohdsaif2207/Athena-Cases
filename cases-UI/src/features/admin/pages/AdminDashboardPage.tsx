import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { FranklinMadisonLogo } from '@/components/branding/FranklinMadisonLogo'
import { BrandWelcomePanel } from '@/components/branding/BrandWelcomePanel'
import { canAccessAdmin } from '@/features/admin/utils/adminAccess'
import {
  adminApi,
  type CaseTypeAdminItem,
  type GroupAdminItem,
  type PermissionItem,
  type RoleAdminItem,
  type TeamAdminItem,
  type UserAdminItem,
} from '@/features/admin/api/adminApi'
import '@/features/cases/pages/CasesDashboardPlaceholder.css'
import './AdminDashboardPage.css'

type UmTab =
  | 'users'
  | 'groups'
  | 'roles'
  | 'permissions'
  | 'teams'
  | 'caseTypes'

const TABS: Array<{ id: UmTab; label: string }> = [
  { id: 'users', label: 'Manage Users' },
  { id: 'groups', label: 'Manage Groups' },
  { id: 'roles', label: 'Manage Roles' },
  { id: 'permissions', label: 'Manage Permissions' },
  { id: 'teams', label: 'Manage Teams' },
  { id: 'caseTypes', label: 'Manage Case Types' },
]

/**
 * Utilities page — full IAM management for admins; Franklin Madison brand for others.
 */
export function AdminDashboardPage() {
  const { user, logout } = useAuth()
  const welcomeName = user?.displayName?.trim() || 'User'
  const isAdmin = canAccessAdmin(user)
  const [tab, setTab] = useState<UmTab>('users')
  const [toast, setToast] = useState<string | null>(null)

  function showToast(message: string) {
    setToast(message)
    window.setTimeout(() => setToast(null), 2800)
  }

  const APP_TABS = [
    'Home',
    'Communications',
    'Products',
    'Revenue Processing',
    'Marketing',
    'Client Services',
    'Response Processing',
    'Claims',
    'DBM',
    'Utilities',
    'Miscellaneous',
    'Legal',
    'Bill Track',
    'Vanity',
    'Forecasting',
    'Cases',
    'Contact Center',
    'Marketing Calendar',
  ] as const

  return (
    <div className="athena-shell" data-testid="utilities-page">
      <header className="athena-topbar">
        <div className="athena-topbar__brand">
          <span className="athena-topbar__app">Athena Nextgen</span>
          <span className="athena-topbar__sep">|</span>
          <span className="athena-topbar__franklin">Franklin</span>
          <FranklinMadisonLogo className="athena-topbar__logo" />
          <span className="athena-topbar__madison">Madison</span>
        </div>
        <div className="athena-topbar__right">
          <p className="athena-topbar__welcome" data-testid="utilities-welcome">
            Welcome, <strong>{welcomeName}</strong>
            <span className="athena-topbar__pipe">|</span>
            <button type="button" className="athena-topbar__logout" onClick={logout} data-testid="utilities-logout">
              Log Out
            </button>
          </p>
        </div>
      </header>

      <div className="athena-tabs-row">
        <nav className="athena-tabs" aria-label="Primary" data-testid="utilities-primary-nav">
          {APP_TABS.map((name) => {
            if (name === 'Home') {
              return (
                <NavLink
                  key={name}
                  to="/home"
                  className={({ isActive }) => `athena-tab ${isActive ? 'is-active' : ''}`}
                  data-testid="nav-home"
                >
                  {name}
                </NavLink>
              )
            }
            if (name === 'Cases') {
              return (
                <NavLink
                  key={name}
                  to="/cases"
                  className={({ isActive }) => `athena-tab ${isActive ? 'is-active' : ''}`}
                  data-testid="nav-cases"
                >
                  {name}
                </NavLink>
              )
            }
            if (name === 'Utilities') {
              return (
                <NavLink
                  key={name}
                  to="/utilities"
                  className="athena-tab is-active"
                  data-testid="nav-utilities"
                >
                  {name}
                </NavLink>
              )
            }
            return (
              <span key={name} className="athena-tab is-inert" title="Not available in this module">
                {name}
              </span>
            )
          })}
        </nav>
        <button type="button" className="athena-other-links" data-testid="other-links">
          Other Links
        </button>
      </div>

      {isAdmin ? (
        <div className="um-subnav" role="tablist" aria-label="Utilities modules" data-testid="utilities-subnav">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              role="tab"
              aria-selected={tab === t.id}
              className={`um-subnav__item ${tab === t.id ? 'is-active' : ''}`}
              onClick={() => setTab(t.id)}
              data-testid={`um-tab-${t.id}`}
            >
              {t.label}
            </button>
          ))}
        </div>
      ) : null}

      <main className="athena-main um-main">
        {isAdmin ? (
          <>
            {tab === 'users' ? <ManageUsersPage onToast={showToast} /> : null}
            {tab === 'groups' ? <GroupsPanel onToast={showToast} /> : null}
            {tab === 'roles' ? <RolesPanel onToast={showToast} /> : null}
            {tab === 'permissions' ? <PermissionsPanel onToast={showToast} /> : null}
            {tab === 'teams' ? <TeamsPanel onToast={showToast} /> : null}
            {tab === 'caseTypes' ? <CaseTypesPanel onToast={showToast} /> : null}
          </>
        ) : (
          <BrandWelcomePanel testId="utilities-brand-welcome" />
        )}
      </main>

      {toast ? (
        <div className="admin-toast" role="status">
          {toast}
        </div>
      ) : null}
    </div>
  )
}

function ManageUsersPage({ onToast }: { onToast: (m: string) => void }) {
  const [users, setUsers] = useState<UserAdminItem[]>([])
  const [groups, setGroups] = useState<GroupAdminItem[]>([])
  const [roles, setRoles] = useState<RoleAdminItem[]>([])
  const [teams, setTeams] = useState<TeamAdminItem[]>([])
  const [permissions, setPermissions] = useState<PermissionItem[]>([])
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const [selectedGroupCode, setSelectedGroupCode] = useState<string | null>(null)
  const [filters, setFilters] = useState({ username: '', displayName: '', status: '' })
  const [draftFilters, setDraftFilters] = useState({ username: '', displayName: '', status: '' })
  const [formOpen, setFormOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({
    username: '',
    displayName: '',
    email: '',
    password: '',
    status: 'ACTIVE',
    roleCodes: [] as string[],
    groupCodes: [] as string[],
    teamCodes: [] as string[],
  })
  const [groupToAdd, setGroupToAdd] = useState('')

  const load = useCallback(async () => {
    const [u, g, r, t, p] = await Promise.all([
      adminApi.listUsers(),
      adminApi.listGroups(),
      adminApi.listRoles(),
      adminApi.listTeams(),
      adminApi.listPermissions(),
    ])
    setUsers(u)
    setGroups(g)
    setRoles(r)
    setTeams(t)
    setPermissions(p)
  }, [])

  useEffect(() => {
    void load().catch(() => onToast('Failed to load user management data.'))
  }, [load, onToast])

  const selectedUser = users.find((u) => u.id === selectedUserId) ?? null

  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      if (filters.username && !u.username.toLowerCase().includes(filters.username.toLowerCase())) {
        return false
      }
      if (
        filters.displayName &&
        !u.displayName.toLowerCase().includes(filters.displayName.toLowerCase())
      ) {
        return false
      }
      if (filters.status && u.status !== filters.status) return false
      return true
    })
  }, [users, filters])

  const userGroups = useMemo(() => {
    if (!selectedUser) return []
    return groups.filter((g) => selectedUser.groupCodes.includes(g.code))
  }, [groups, selectedUser])

  const selectedGroup = groups.find((g) => g.code === selectedGroupCode) ?? null

  const groupPermissions = useMemo(() => {
    if (!selectedGroup) return []
    const codes = new Set<string>()
    for (const roleCode of selectedGroup.roleCodes) {
      const role = roles.find((r) => r.code === roleCode)
      role?.permissionCodes.forEach((c) => codes.add(c))
    }
    return permissions.filter((p) => codes.has(p.code))
  }, [selectedGroup, roles, permissions])

  function openCreate() {
    setCreating(true)
    setFormOpen(true)
    setForm({
      username: '',
      displayName: '',
      email: '',
      password: '',
      status: 'ACTIVE',
      roleCodes: [],
      groupCodes: [],
      teamCodes: [],
    })
  }

  function openEdit(user: UserAdminItem) {
    setCreating(false)
    setFormOpen(true)
    setSelectedUserId(user.id)
    setForm({
      username: user.username,
      displayName: user.displayName,
      email: user.email ?? '',
      password: '',
      status: user.status,
      roleCodes: [...user.roleCodes],
      groupCodes: [...user.groupCodes],
      teamCodes: [...user.teamCodes],
    })
  }

  async function saveUser() {
    try {
      const body = {
        username: form.username,
        displayName: form.displayName,
        email: form.email || null,
        password: form.password || null,
        status: form.status,
        roleCodes: form.roleCodes,
        groupCodes: form.groupCodes,
        teamCodes: form.teamCodes,
      }
      if (creating) {
        const created = await adminApi.createUser(body)
        setSelectedUserId(created.id)
        onToast('User created.')
      } else if (selectedUser) {
        await adminApi.updateUser(selectedUser.id, body)
        onToast('User updated.')
      }
      setFormOpen(false)
      await load()
    } catch {
      onToast('Unable to save user.')
    }
  }

  async function persistUserGroups(nextGroupCodes: string[]) {
    if (!selectedUser) return
    try {
      await adminApi.updateUser(selectedUser.id, {
        username: selectedUser.username,
        displayName: selectedUser.displayName,
        email: selectedUser.email,
        password: null,
        status: selectedUser.status,
        roleCodes: selectedUser.roleCodes,
        groupCodes: nextGroupCodes,
        teamCodes: selectedUser.teamCodes,
      })
      await load()
    } catch {
      onToast('Unable to update user groups.')
    }
  }

  async function addGroupToUser() {
    if (!selectedUser || !groupToAdd) return
    if (selectedUser.groupCodes.includes(groupToAdd)) {
      onToast('Group already assigned.')
      return
    }
    await persistUserGroups([...selectedUser.groupCodes, groupToAdd])
    setSelectedGroupCode(groupToAdd)
    setGroupToAdd('')
    onToast('Group added to user.')
  }

  async function removeGroupFromUser(code: string) {
    if (!selectedUser) return
    const next = selectedUser.groupCodes.filter((c) => c !== code)
    await persistUserGroups(next)
    if (selectedGroupCode === code) setSelectedGroupCode(null)
    onToast('Group removed from user.')
  }

  async function togglePermissionOnGroupRoles(permissionCode: string, assign: boolean) {
    if (!selectedGroup) return
    try {
      for (const roleCode of selectedGroup.roleCodes) {
        const role = roles.find((r) => r.code === roleCode)
        if (!role) continue
        const has = role.permissionCodes.includes(permissionCode)
        if (assign && has) continue
        if (!assign && !has) continue
        const permissionCodes = assign
          ? [...role.permissionCodes, permissionCode]
          : role.permissionCodes.filter((c) => c !== permissionCode)
        await adminApi.updateRole(role.id, {
          code: role.code,
          name: role.name,
          description: role.description,
          active: role.active,
          permissionCodes,
          caseTypeCodes: role.caseTypeCodes,
          initiatingTeamCodes: role.initiatingTeamCodes,
          receivingTeamCodes: role.receivingTeamCodes,
        })
      }
      await load()
      onToast(assign ? 'Permission added via group roles.' : 'Permission removed from group roles.')
    } catch {
      onToast('Unable to update permissions.')
    }
  }

  const availableGroupsToAdd = groups.filter(
    (g) => g.active && !(selectedUser?.groupCodes.includes(g.code) ?? false),
  )

  return (
    <div className="um-users" data-testid="manage-users-page">
      <div className="um-toolbar">
        <button type="button" className="admin-btn admin-btn--primary" onClick={openCreate}>
          Add User
        </button>
        <button
          type="button"
          className="admin-btn admin-btn--primary"
          disabled={!selectedUser}
          onClick={() => selectedUser && openEdit(selectedUser)}
        >
          Edit User
        </button>
        <button
          type="button"
          className="admin-btn admin-btn--primary"
          disabled={!selectedUser || selectedUser.status === 'ACTIVE'}
          onClick={() =>
            selectedUser &&
            void adminApi
              .setUserStatus(selectedUser.id, 'ACTIVE')
              .then(load)
              .then(() => onToast('User activated.'))
              .catch(() => onToast('Activate failed.'))
          }
        >
          Activate
        </button>
        <button
          type="button"
          className="admin-btn admin-btn--primary"
          disabled={!selectedUser || selectedUser.status !== 'ACTIVE'}
          onClick={() =>
            selectedUser &&
            void adminApi
              .setUserStatus(selectedUser.id, 'INACTIVE')
              .then(load)
              .then(() => onToast('User deactivated.'))
              .catch(() => onToast('Deactivate failed.'))
          }
        >
          Deactivate
        </button>
        <button
          type="button"
          className="admin-btn admin-btn--primary"
          disabled={!selectedUser}
          onClick={() => {
            if (!selectedUser) return
            const pwd = window.prompt('Enter new password (min 8 characters)')
            if (!pwd) return
            void adminApi
              .resetPassword(selectedUser.id, pwd)
              .then(() => onToast('Password reset.'))
              .catch(() => onToast('Password reset failed.'))
          }}
        >
          Reset Password
        </button>
        <button
          type="button"
          className="admin-btn admin-btn--primary"
          onClick={() => setFilters({ ...draftFilters })}
        >
          Search
        </button>
        <button
          type="button"
          className="admin-btn admin-btn--primary"
          onClick={() => {
            setDraftFilters({ username: '', displayName: '', status: '' })
            setFilters({ username: '', displayName: '', status: '' })
          }}
        >
          Clear Filters
        </button>
      </div>

      <div className="um-tri">
        <section className="um-pane" data-testid="um-users-grid">
          <h3 className="um-pane__title">Users</h3>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Actions</th>
                  <th>Username</th>
                  <th>Display Name</th>
                  <th>Status</th>
                  <th>Role(s)</th>
                  <th>Group(s)</th>
                  <th>Team(s)</th>
                  <th>Case Type(s)</th>
                  <th>Created Date</th>
                  <th>Last Updated</th>
                </tr>
                <tr className="um-filter-row">
                  <th />
                  <th>
                    <input
                      value={draftFilters.username}
                      onChange={(e) => setDraftFilters({ ...draftFilters, username: e.target.value })}
                      placeholder="Filter"
                    />
                  </th>
                  <th>
                    <input
                      value={draftFilters.displayName}
                      onChange={(e) =>
                        setDraftFilters({ ...draftFilters, displayName: e.target.value })
                      }
                      placeholder="Filter"
                    />
                  </th>
                  <th>
                    <select
                      value={draftFilters.status}
                      onChange={(e) => setDraftFilters({ ...draftFilters, status: e.target.value })}
                    >
                      <option value="">All</option>
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                      <option value="LOCKED">LOCKED</option>
                    </select>
                  </th>
                  <th colSpan={6} />
                </tr>
              </thead>
              <tbody>
                {filteredUsers.length === 0 ? (
                  <tr>
                    <td colSpan={10} className="admin-empty">
                      No users found.
                    </td>
                  </tr>
                ) : (
                  filteredUsers.map((u, i) => (
                    <tr
                      key={u.id}
                      className={`${i % 2 === 1 ? 'is-alt' : ''} ${selectedUserId === u.id ? 'is-selected' : ''}`}
                      onClick={() => {
                        setSelectedUserId(u.id)
                        setSelectedGroupCode(u.groupCodes[0] ?? null)
                        setFormOpen(false)
                      }}
                    >
                      <td>
                        <button
                          type="button"
                          className="um-icon-btn"
                          title="Edit"
                          onClick={(e) => {
                            e.stopPropagation()
                            openEdit(u)
                          }}
                        >
                          ✎
                        </button>
                      </td>
                      <td>{u.username}</td>
                      <td>{u.displayName}</td>
                      <td>{u.status}</td>
                      <td>{u.roleCodes.join(', ')}</td>
                      <td>{u.groupCodes.join(', ')}</td>
                      <td>{u.teamCodes.join(', ')}</td>
                      <td>{u.effectiveCaseTypeCodes.join(', ')}</td>
                      <td>{formatDate(u.createdAt)}</td>
                      <td>{formatDate(u.updatedAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="um-pane" data-testid="um-groups-grid">
          <h3 className="um-pane__title">Associated Groups To User</h3>
          <div className="um-inline-add">
            <select
              value={groupToAdd}
              disabled={!selectedUser}
              onChange={(e) => setGroupToAdd(e.target.value)}
            >
              <option value="">Select Group</option>
              {availableGroupsToAdd.map((g) => (
                <option key={g.code} value={g.code}>
                  {g.code} — {g.name}
                </option>
              ))}
            </select>
            <button
              type="button"
              className="admin-btn admin-btn--primary"
              disabled={!selectedUser || !groupToAdd}
              onClick={() => void addGroupToUser()}
            >
              Add
            </button>
          </div>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Group ID</th>
                  <th>Group Name</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody>
                {!selectedUser ? (
                  <tr>
                    <td colSpan={4} className="admin-empty">
                      Select a user to view groups.
                    </td>
                  </tr>
                ) : userGroups.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="admin-empty">
                      No groups assigned.
                    </td>
                  </tr>
                ) : (
                  userGroups.map((g, i) => (
                    <tr
                      key={g.id}
                      className={`${i % 2 === 1 ? 'is-alt' : ''} ${selectedGroupCode === g.code ? 'is-selected' : ''}`}
                      onClick={() => setSelectedGroupCode(g.code)}
                    >
                      <td>
                        <button
                          type="button"
                          className="um-icon-btn um-icon-btn--danger"
                          title="Remove group"
                          onClick={(e) => {
                            e.stopPropagation()
                            void removeGroupFromUser(g.code)
                          }}
                        >
                          🗑
                        </button>
                      </td>
                      <td>{g.id}</td>
                      <td>{g.name}</td>
                      <td>{g.description ?? ''}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="um-pane" data-testid="um-rights-grid">
          <h3 className="um-pane__title">Associated Rights / Permissions</h3>
          <div className="um-inline-add">
            <select
              id="perm-add"
              disabled={!selectedGroup}
              defaultValue=""
              onChange={(e) => {
                const code = e.target.value
                if (!code) return
                void togglePermissionOnGroupRoles(code, true)
                e.target.value = ''
              }}
            >
              <option value="">Add Permission</option>
              {permissions
                .filter((p) => !groupPermissions.some((gp) => gp.code === p.code))
                .map((p) => (
                  <option key={p.code} value={p.code}>
                    {p.code} — {p.name}
                  </option>
                ))}
            </select>
          </div>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Permission Code</th>
                  <th>Permission Name</th>
                  <th>Description</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {!selectedGroup ? (
                  <tr>
                    <td colSpan={5} className="admin-empty">
                      Select a group to view permissions.
                    </td>
                  </tr>
                ) : groupPermissions.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="admin-empty">
                      No permissions for this group&apos;s roles.
                    </td>
                  </tr>
                ) : (
                  groupPermissions.map((p, i) => (
                    <tr key={p.id} className={i % 2 === 1 ? 'is-alt' : undefined}>
                      <td>
                        <button
                          type="button"
                          className="um-icon-btn um-icon-btn--danger"
                          title="Remove permission"
                          onClick={() => void togglePermissionOnGroupRoles(p.code, false)}
                        >
                          🗑
                        </button>
                      </td>
                      <td>{p.code}</td>
                      <td>{p.name}</td>
                      <td>{p.description ?? ''}</td>
                      <td>A - ACTIVE</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {formOpen ? (
        <AdminForm
          title={creating ? 'Add User' : `Edit User — ${form.username}`}
          onCancel={() => setFormOpen(false)}
          onSave={() => void saveUser()}
        >
          <Field label="Username">
            <input
              value={form.username}
              disabled={!creating}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
            />
          </Field>
          <Field label="Display Name">
            <input
              value={form.displayName}
              onChange={(e) => setForm({ ...form, displayName: e.target.value })}
            />
          </Field>
          <Field label="Email">
            <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </Field>
          <Field label={creating ? 'Password' : 'Password (optional)'}>
            <input
              type="password"
              autoComplete="new-password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder={creating ? '' : 'Leave blank to keep current password'}
            />
          </Field>
          <Field label="Status">
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="LOCKED">LOCKED</option>
            </select>
          </Field>
          <CodeChecklist
            label="Roles"
            options={roles.map((r) => r.code)}
            value={form.roleCodes}
            onChange={(roleCodes) => setForm({ ...form, roleCodes })}
          />
          <CodeChecklist
            label="Groups"
            options={groups.map((g) => g.code)}
            value={form.groupCodes}
            onChange={(groupCodes) => setForm({ ...form, groupCodes })}
          />
          <CodeChecklist
            label="Teams"
            options={teams.map((t) => t.code)}
            value={form.teamCodes}
            onChange={(teamCodes) => setForm({ ...form, teamCodes })}
          />
        </AdminForm>
      ) : null}
    </div>
  )
}

function GroupsPanel({ onToast }: { onToast: (m: string) => void }) {
  const [rows, setRows] = useState<GroupAdminItem[]>([])
  const [roles, setRoles] = useState<RoleAdminItem[]>([])
  const [users, setUsers] = useState<UserAdminItem[]>([])
  const [editing, setEditing] = useState<GroupAdminItem | null>(null)
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({
    code: '',
    name: '',
    description: '',
    active: true,
    roleCodes: [] as string[],
    usernames: [] as string[],
  })

  const load = useCallback(async () => {
    const [g, r, u] = await Promise.all([
      adminApi.listGroups(),
      adminApi.listRoles(),
      adminApi.listUsers(),
    ])
    setRows(g)
    setRoles(r)
    setUsers(u)
  }, [])

  useEffect(() => {
    void load().catch(() => onToast('Failed to load groups.'))
  }, [load, onToast])

  async function save() {
    try {
      const body = { ...form, description: form.description || null }
      if (creating) await adminApi.createGroup(body)
      else if (editing) await adminApi.updateGroup(editing.id, body)
      onToast(creating ? 'Group created.' : 'Group updated.')
      setCreating(false)
      setEditing(null)
      await load()
    } catch {
      onToast('Unable to save group.')
    }
  }

  return (
    <Panel
      title="Manage Groups"
      onCreate={() => {
        setCreating(true)
        setEditing(null)
        setForm({ code: '', name: '', description: '', active: true, roleCodes: [], usernames: [] })
      }}
    >
      <AdminTable
        headers={['Code', 'Name', 'Active', 'Roles', 'Users', 'Actions']}
        rows={rows.map((r) => [
          r.code,
          r.name,
          r.active ? 'Yes' : 'No',
          r.roleCodes.join(', '),
          r.usernames.join(', '),
          <span key={r.id} className="admin-actions">
            <button
              type="button"
              className="admin-btn"
              onClick={() => {
                setCreating(false)
                setEditing(r)
                setForm({
                  code: r.code,
                  name: r.name,
                  description: r.description ?? '',
                  active: r.active,
                  roleCodes: [...r.roleCodes],
                  usernames: [...r.usernames],
                })
              }}
            >
              Edit
            </button>
            <button
              type="button"
              className="admin-btn"
              disabled={r.code === 'SYSTEM_ADMINISTRATORS'}
              onClick={() =>
                void adminApi
                  .deleteGroup(r.id)
                  .then(load)
                  .then(() => onToast('Group deactivated.'))
                  .catch(() => onToast('Delete failed.'))
              }
            >
              Delete
            </button>
          </span>,
        ])}
      />
      {(creating || editing) && (
        <AdminForm
          title={creating ? 'Create Group' : `Edit Group — ${editing?.code}`}
          onCancel={() => {
            setCreating(false)
            setEditing(null)
          }}
          onSave={() => void save()}
        >
          <Field label="Code">
            <input
              value={form.code}
              disabled={!creating}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
            />
          </Field>
          <Field label="Name">
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </Field>
          <Field label="Description">
            <input
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </Field>
          <label className="admin-check">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Active
          </label>
          <CodeChecklist
            label="Roles"
            options={roles.map((r) => r.code)}
            value={form.roleCodes}
            onChange={(roleCodes) => setForm({ ...form, roleCodes })}
          />
          <CodeChecklist
            label="Users"
            options={users.map((u) => u.username)}
            value={form.usernames}
            onChange={(usernames) => setForm({ ...form, usernames })}
          />
        </AdminForm>
      )}
    </Panel>
  )
}

function RolesPanel({ onToast }: { onToast: (m: string) => void }) {
  const [rows, setRows] = useState<RoleAdminItem[]>([])
  const [permissions, setPermissions] = useState<PermissionItem[]>([])
  const [caseTypes, setCaseTypes] = useState<CaseTypeAdminItem[]>([])
  const [teams, setTeams] = useState<TeamAdminItem[]>([])
  const [editing, setEditing] = useState<RoleAdminItem | null>(null)
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({
    code: '',
    name: '',
    description: '',
    active: true,
    permissionCodes: [] as string[],
    caseTypeCodes: [] as string[],
    initiatingTeamCodes: [] as string[],
    receivingTeamCodes: [] as string[],
  })

  const initiatingOptions = useMemo(
    () => teams.filter((t) => t.teamType === 'INITIATING' || t.teamType === 'BOTH').map((t) => t.code),
    [teams],
  )
  const receivingOptions = useMemo(
    () => teams.filter((t) => t.teamType === 'RECEIVING' || t.teamType === 'BOTH').map((t) => t.code),
    [teams],
  )

  const load = useCallback(async () => {
    const [r, p, c, t] = await Promise.all([
      adminApi.listRoles(),
      adminApi.listPermissions(),
      adminApi.listCaseTypes(),
      adminApi.listTeams(),
    ])
    setRows(r)
    setPermissions(p)
    setCaseTypes(c)
    setTeams(t)
  }, [])

  useEffect(() => {
    void load().catch(() => onToast('Failed to load roles.'))
  }, [load, onToast])

  async function save() {
    try {
      const body = { ...form, description: form.description || null }
      if (creating) await adminApi.createRole(body)
      else if (editing) await adminApi.updateRole(editing.id, body)
      onToast(creating ? 'Role created.' : 'Role updated.')
      setCreating(false)
      setEditing(null)
      await load()
    } catch {
      onToast('Unable to save role.')
    }
  }

  return (
    <Panel
      title="Manage Roles"
      onCreate={() => {
        setCreating(true)
        setEditing(null)
        setForm({
          code: '',
          name: '',
          description: '',
          active: true,
          permissionCodes: [],
          caseTypeCodes: [],
          initiatingTeamCodes: [],
          receivingTeamCodes: [],
        })
      }}
    >
      <AdminTable
        headers={['Code', 'Name', 'Active', 'Permissions', 'Case Types', 'Actions']}
        rows={rows.map((r) => [
          r.code,
          r.name,
          r.active ? 'Yes' : 'No',
          r.permissionCodes.join(', '),
          r.caseTypeCodes.join(', '),
          <span key={r.id} className="admin-actions">
            <button
              type="button"
              className="admin-btn"
              onClick={() => {
                setCreating(false)
                setEditing(r)
                setForm({
                  code: r.code,
                  name: r.name,
                  description: r.description ?? '',
                  active: r.active,
                  permissionCodes: [...r.permissionCodes],
                  caseTypeCodes: [...r.caseTypeCodes],
                  initiatingTeamCodes: [...r.initiatingTeamCodes],
                  receivingTeamCodes: [...r.receivingTeamCodes],
                })
              }}
            >
              Edit
            </button>
            <button
              type="button"
              className="admin-btn"
              onClick={() =>
                void adminApi
                  .updateRole(r.id, {
                    code: r.code,
                    name: r.name,
                    description: r.description,
                    active: !r.active,
                    permissionCodes: r.permissionCodes,
                    caseTypeCodes: r.caseTypeCodes,
                    initiatingTeamCodes: r.initiatingTeamCodes,
                    receivingTeamCodes: r.receivingTeamCodes,
                  })
                  .then(load)
                  .then(() => onToast(r.active ? 'Role deactivated.' : 'Role activated.'))
              }
            >
              {r.active ? 'Deactivate' : 'Activate'}
            </button>
          </span>,
        ])}
      />
      {(creating || editing) && (
        <AdminForm
          title={creating ? 'Create Role' : `Edit Role — ${editing?.code}`}
          onCancel={() => {
            setCreating(false)
            setEditing(null)
          }}
          onSave={() => void save()}
        >
          <Field label="Code">
            <input
              value={form.code}
              disabled={!creating}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
            />
          </Field>
          <Field label="Name">
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </Field>
          <Field label="Description">
            <input
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </Field>
          <label className="admin-check">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Active
          </label>
          <CodeChecklist
            label="Permissions"
            options={permissions.map((p) => p.code)}
            value={form.permissionCodes}
            onChange={(permissionCodes) => setForm({ ...form, permissionCodes })}
          />
          <CodeChecklist
            label="Case Types"
            options={caseTypes.map((c) => c.code)}
            value={form.caseTypeCodes}
            onChange={(caseTypeCodes) => setForm({ ...form, caseTypeCodes })}
          />
          <CodeChecklist
            label="Initiating Teams"
            options={initiatingOptions}
            value={form.initiatingTeamCodes}
            onChange={(initiatingTeamCodes) => setForm({ ...form, initiatingTeamCodes })}
          />
          <CodeChecklist
            label="Receiving Teams"
            options={receivingOptions}
            value={form.receivingTeamCodes}
            onChange={(receivingTeamCodes) => setForm({ ...form, receivingTeamCodes })}
          />
        </AdminForm>
      )}
    </Panel>
  )
}

function PermissionsPanel({ onToast }: { onToast: (m: string) => void }) {
  const [permissions, setPermissions] = useState<PermissionItem[]>([])
  const [roles, setRoles] = useState<RoleAdminItem[]>([])
  const [selectedRole, setSelectedRole] = useState('')

  useEffect(() => {
    void (async () => {
      try {
        const [p, r] = await Promise.all([adminApi.listPermissions(), adminApi.listRoles()])
        setPermissions(p)
        setRoles(r)
        setSelectedRole((prev) => prev || r[0]?.code || '')
      } catch {
        onToast('Failed to load permissions.')
      }
    })()
  }, [onToast])

  const role = roles.find((r) => r.code === selectedRole)

  async function togglePermission(code: string) {
    if (!role) return
    const next = role.permissionCodes.includes(code)
      ? role.permissionCodes.filter((c) => c !== code)
      : [...role.permissionCodes, code]
    try {
      await adminApi.updateRole(role.id, {
        code: role.code,
        name: role.name,
        description: role.description,
        active: role.active,
        permissionCodes: next,
        caseTypeCodes: role.caseTypeCodes,
        initiatingTeamCodes: role.initiatingTeamCodes,
        receivingTeamCodes: role.receivingTeamCodes,
      })
      setRoles(await adminApi.listRoles())
      onToast('Role permissions updated.')
    } catch {
      onToast('Unable to update permissions.')
    }
  }

  return (
    <Panel title="Manage Permissions">
      <p className="admin-hint">Assign or remove permissions on a role (database-driven RBAC).</p>
      <Field label="Role">
        <select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)}>
          {roles.map((r) => (
            <option key={r.code} value={r.code}>
              {r.code} — {r.name}
            </option>
          ))}
        </select>
      </Field>
      <AdminTable
        headers={['Code', 'Name', 'Description', 'Assigned']}
        rows={permissions.map((p) => [
          p.code,
          p.name,
          p.description ?? '',
          <label key={p.id} className="admin-check">
            <input
              type="checkbox"
              checked={role?.permissionCodes.includes(p.code) ?? false}
              onChange={() => void togglePermission(p.code)}
            />
            {role?.permissionCodes.includes(p.code) ? 'Yes' : 'No'}
          </label>,
        ])}
      />
    </Panel>
  )
}

function CaseTypesPanel({ onToast }: { onToast: (m: string) => void }) {
  const [rows, setRows] = useState<CaseTypeAdminItem[]>([])
  const [teams, setTeams] = useState<TeamAdminItem[]>([])
  const [editing, setEditing] = useState<CaseTypeAdminItem | null>(null)
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({
    code: '',
    name: '',
    moduleKey: '',
    active: true,
    initiatingTeamCodes: [] as string[],
    receivingTeamCodes: [] as string[],
  })

  const initiatingOptions = useMemo(
    () => teams.filter((t) => t.teamType === 'INITIATING' || t.teamType === 'BOTH').map((t) => t.code),
    [teams],
  )
  const receivingOptions = useMemo(
    () => teams.filter((t) => t.teamType === 'RECEIVING' || t.teamType === 'BOTH').map((t) => t.code),
    [teams],
  )

  const load = useCallback(async () => {
    const [c, t] = await Promise.all([adminApi.listCaseTypes(), adminApi.listTeams()])
    setRows(c)
    setTeams(t)
  }, [])

  useEffect(() => {
    void load().catch(() => onToast('Failed to load case types.'))
  }, [load, onToast])

  async function save() {
    try {
      if (creating) await adminApi.createCaseType(form)
      else if (editing) await adminApi.updateCaseType(editing.id, form)
      onToast(creating ? 'Case type created.' : 'Case type updated.')
      setCreating(false)
      setEditing(null)
      await load()
    } catch {
      onToast('Unable to save case type.')
    }
  }

  return (
    <Panel
      title="Manage Case Types"
      onCreate={() => {
        setCreating(true)
        setEditing(null)
        setForm({
          code: '',
          name: '',
          moduleKey: '',
          active: true,
          initiatingTeamCodes: [],
          receivingTeamCodes: [],
        })
      }}
    >
      <AdminTable
        headers={['Code', 'Name', 'Module', 'Initiating Teams', 'Receiving Teams', 'Actions']}
        rows={rows.map((r) => [
          r.code,
          r.name,
          r.moduleKey,
          r.initiatingTeamCodes.join(', '),
          r.receivingTeamCodes.join(', '),
          <button
            key={r.id}
            type="button"
            className="admin-btn"
            onClick={() => {
              setCreating(false)
              setEditing(r)
              setForm({
                code: r.code,
                name: r.name,
                moduleKey: r.moduleKey,
                active: r.active,
                initiatingTeamCodes: [...r.initiatingTeamCodes],
                receivingTeamCodes: [...r.receivingTeamCodes],
              })
            }}
          >
            Edit
          </button>,
        ])}
      />
      {(creating || editing) && (
        <AdminForm
          title={creating ? 'Create Case Type' : `Edit Case Type — ${editing?.code}`}
          onCancel={() => {
            setCreating(false)
            setEditing(null)
          }}
          onSave={() => void save()}
        >
          <Field label="Code">
            <input
              value={form.code}
              disabled={!creating}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
            />
          </Field>
          <Field label="Name">
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </Field>
          <Field label="Module Key">
            <input
              value={form.moduleKey}
              onChange={(e) => setForm({ ...form, moduleKey: e.target.value })}
            />
          </Field>
          <label className="admin-check">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Active
          </label>
          <CodeChecklist
            label="Initiating Teams"
            options={initiatingOptions}
            value={form.initiatingTeamCodes}
            onChange={(initiatingTeamCodes) => setForm({ ...form, initiatingTeamCodes })}
          />
          <CodeChecklist
            label="Receiving Teams"
            options={receivingOptions}
            value={form.receivingTeamCodes}
            onChange={(receivingTeamCodes) => setForm({ ...form, receivingTeamCodes })}
          />
        </AdminForm>
      )}
    </Panel>
  )
}

function TeamsPanel({ onToast }: { onToast: (m: string) => void }) {
  const [rows, setRows] = useState<TeamAdminItem[]>([])
  const [editing, setEditing] = useState<TeamAdminItem | null>(null)
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({
    code: '',
    name: '',
    teamType: 'INITIATING',
    active: true,
  })

  const load = useCallback(async () => {
    setRows(await adminApi.listTeams())
  }, [])

  useEffect(() => {
    void load().catch(() => onToast('Failed to load teams.'))
  }, [load, onToast])

  async function save() {
    try {
      if (creating) await adminApi.createTeam(form)
      else if (editing) await adminApi.updateTeam(editing.id, form)
      onToast(creating ? 'Team created.' : 'Team updated.')
      setCreating(false)
      setEditing(null)
      await load()
    } catch {
      onToast('Unable to save team.')
    }
  }

  return (
    <Panel
      title="Manage Teams"
      onCreate={() => {
        setCreating(true)
        setEditing(null)
        setForm({ code: '', name: '', teamType: 'INITIATING', active: true })
      }}
    >
      <AdminTable
        headers={['Code', 'Name', 'Type', 'Active', 'Actions']}
        rows={rows.map((r) => [
          r.code,
          r.name,
          r.teamType,
          r.active ? 'Yes' : 'No',
          <span key={r.id} className="admin-actions">
            <button
              type="button"
              className="admin-btn"
              onClick={() => {
                setCreating(false)
                setEditing(r)
                setForm({ code: r.code, name: r.name, teamType: r.teamType, active: r.active })
              }}
            >
              Edit
            </button>
            <button
              type="button"
              className="admin-btn"
              onClick={() =>
                void adminApi
                  .updateTeam(r.id, {
                    code: r.code,
                    name: r.name,
                    teamType: r.teamType,
                    active: !r.active,
                  })
                  .then(load)
                  .then(() => onToast(r.active ? 'Team deactivated.' : 'Team activated.'))
              }
            >
              {r.active ? 'Deactivate' : 'Activate'}
            </button>
          </span>,
        ])}
      />
      {(creating || editing) && (
        <AdminForm
          title={creating ? 'Create Team' : `Edit Team — ${editing?.code}`}
          onCancel={() => {
            setCreating(false)
            setEditing(null)
          }}
          onSave={() => void save()}
        >
          <Field label="Code">
            <input
              value={form.code}
              disabled={!creating}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
            />
          </Field>
          <Field label="Name">
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </Field>
          <Field label="Team Type">
            <select
              value={form.teamType}
              onChange={(e) => setForm({ ...form, teamType: e.target.value })}
            >
              <option value="INITIATING">INITIATING</option>
              <option value="RECEIVING">RECEIVING</option>
              <option value="BOTH">BOTH</option>
            </select>
          </Field>
          <label className="admin-check">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Active
          </label>
        </AdminForm>
      )}
    </Panel>
  )
}

function Panel({
  title,
  onCreate,
  children,
}: {
  title: string
  onCreate?: () => void
  children: ReactNode
}) {
  return (
    <div className="admin-module admin-panel" data-testid="admin-module">
      <div className="admin-module__header">
        <h2>{title}</h2>
        {onCreate ? (
          <button type="button" className="admin-btn admin-btn--primary" onClick={onCreate}>
            Create
          </button>
        ) : null}
      </div>
      {children}
    </div>
  )
}

function AdminTable({ headers, rows }: { headers: string[]; rows: ReactNode[][] }) {
  return (
    <div className="admin-table-wrap">
      <table className="admin-table">
        <thead>
          <tr>
            {headers.map((h) => (
              <th key={h}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={headers.length} className="admin-empty">
                No records.
              </td>
            </tr>
          ) : (
            rows.map((cells, i) => (
              <tr key={i} className={i % 2 === 1 ? 'is-alt' : undefined}>
                {cells.map((cell, j) => (
                  <td key={j}>{cell}</td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}

function AdminForm({
  title,
  onCancel,
  onSave,
  children,
}: {
  title: string
  onCancel: () => void
  onSave: () => void
  children: ReactNode
}) {
  return (
    <div className="admin-form" data-testid="admin-form">
      <h3>{title}</h3>
      <div className="admin-form__grid">{children}</div>
      <div className="admin-form__actions">
        <button type="button" className="admin-btn" onClick={onCancel}>
          Cancel
        </button>
        <button type="button" className="admin-btn admin-btn--primary" onClick={onSave}>
          Save
        </button>
      </div>
    </div>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="admin-field">
      <span>{label}</span>
      {children}
    </label>
  )
}

function CodeChecklist({
  label,
  options,
  value,
  onChange,
}: {
  label: string
  options: string[]
  value: string[]
  onChange: (next: string[]) => void
}) {
  return (
    <fieldset className="admin-checklist">
      <legend>{label}</legend>
      <div className="admin-checklist__items">
        {options.map((code) => {
          const checked = value.includes(code)
          return (
            <label key={code} className="admin-check">
              <input
                type="checkbox"
                checked={checked}
                onChange={() =>
                  onChange(checked ? value.filter((v) => v !== code) : [...value, code])
                }
              />
              {code}
            </label>
          )
        })}
      </div>
    </fieldset>
  )
}

function formatDate(iso: string | null | undefined): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${mm}/${dd}/${d.getFullYear()}`
}
