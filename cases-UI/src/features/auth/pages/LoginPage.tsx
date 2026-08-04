import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { FranklinMadisonLogo } from '@/components/branding/FranklinMadisonLogo'
import { useAuth } from '@/contexts/AuthContext'
import { getErrorMessage } from '@/lib/errors'
import './LoginPage.css'

export function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [usernameError, setUsernameError] = useState<string | null>(null)
  const [passwordError, setPasswordError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/home" replace />
  }

  function validate(): boolean {
    let ok = true
    if (!username.trim()) {
      setUsernameError('Username is required.')
      ok = false
    } else {
      setUsernameError(null)
    }
    if (!password) {
      setPasswordError('Password is required.')
      ok = false
    } else {
      setPasswordError(null)
    }
    return ok
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError(null)
    if (!validate()) {
      return
    }

    setSubmitting(true)
    try {
      await login(username.trim(), password)
      navigate('/home', { replace: true })
    } catch (error) {
      setFormError(getErrorMessage(error, 'Unable to sign in. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-page" data-testid="login-page">
      <div className="login-page__backdrop" aria-hidden="true" />

      <main className="login-card" role="main">
        <header className="login-card__brand">
          <div className="login-card__brand-inner">
            <span className="login-card__brand-text">FRANKLIN</span>
            <FranklinMadisonLogo className="login-card__logo" />
            <span className="login-card__brand-text">MADISON</span>
          </div>
        </header>

        <section className="login-card__body">
          <h1 className="login-card__title">Athena Nextgen — Login</h1>
          <p className="login-card__subtitle">Sign in with your Franklin Madison credentials</p>

          <form className="login-form" onSubmit={onSubmit} noValidate data-testid="login-form">
            {formError ? (
              <div className="login-form__alert" role="alert" data-testid="login-error">
                {formError}
              </div>
            ) : null}

            <div className="login-field">
              <label htmlFor="username">Username</label>
              <div className="login-field__control">
                <span className="login-field__icon" aria-hidden="true">
                  <UserIcon />
                </span>
                <input
                  id="username"
                  name="username"
                  type="text"
                  autoComplete="username"
                  value={username}
                  onChange={(e) => {
                    setUsername(e.target.value)
                    if (usernameError) setUsernameError(null)
                  }}
                  aria-invalid={Boolean(usernameError)}
                  aria-describedby={usernameError ? 'username-error' : undefined}
                  data-testid="login-username"
                  disabled={submitting}
                />
              </div>
              {usernameError ? (
                <p id="username-error" className="login-field__error" role="alert">
                  {usernameError}
                </p>
              ) : null}
            </div>

            <div className="login-field">
              <label htmlFor="password">Password</label>
              <div className="login-field__control">
                <span className="login-field__icon" aria-hidden="true">
                  <LockIcon />
                </span>
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value)
                    if (passwordError) setPasswordError(null)
                  }}
                  aria-invalid={Boolean(passwordError)}
                  aria-describedby={passwordError ? 'password-error' : undefined}
                  data-testid="login-password"
                  disabled={submitting}
                />
                <button
                  type="button"
                  className="login-field__toggle"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  data-testid="login-toggle-password"
                  disabled={submitting}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {passwordError ? (
                <p id="password-error" className="login-field__error" role="alert">
                  {passwordError}
                </p>
              ) : null}
            </div>

            <button
              type="submit"
              className="login-submit"
              data-testid="login-submit"
              disabled={submitting}
            >
              {submitting ? 'Signing in…' : 'Login'}
            </button>
          </form>
        </section>
      </main>
    </div>
  )
}

function UserIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 12a4.5 4.5 0 1 0-4.5-4.5A4.5 4.5 0 0 0 12 12Zm0 2.25c-4.14 0-7.5 2.1-7.5 4.69V21h15v-2.06c0-2.59-3.36-4.69-7.5-4.69Z"
        fill="currentColor"
      />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M17 9h-1V7a4 4 0 1 0-8 0v2H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2Zm-6-2a2 2 0 1 1 4 0v2h-4V7Zm6 12H7v-8h10v8Z"
        fill="currentColor"
      />
    </svg>
  )
}

function EyeIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 5c-5 0-9.27 3.11-11 7 1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7Zm0 12a5 5 0 1 1 5-5 5 5 0 0 1-5 5Zm0-8a3 3 0 1 0 3 3 3 3 0 0 0-3-3Z"
        fill="currentColor"
      />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="m3.28 2.22-1.06 1.06 3.1 3.1C3.5 7.62 2.1 9.18 1 12c1.73 3.89 6 7 11 7a12.3 12.3 0 0 0 5.3-1.2l3.42 3.42 1.06-1.06ZM12 17c-3.87 0-7.19-2.25-8.82-5a9.7 9.7 0 0 1 3.2-3.36l1.6 1.6A4.98 4.98 0 0 0 7 12a5 5 0 0 0 5 5 4.98 4.98 0 0 0 1.76-.32l1.55 1.55A10.4 10.4 0 0 1 12 17Zm9.82-5A13.4 13.4 0 0 0 17.7 7.7l-1.48 1.48A9.6 9.6 0 0 1 20.82 12c-.7 1.58-2 3.05-3.64 4.1l1.45 1.45C20.7 16.1 22.3 14.2 23 12ZM9.88 6.7 11.5 8.3A5 5 0 0 1 16.7 13.5l1.55 1.55A7 7 0 0 0 9.88 6.7Z"
        fill="currentColor"
      />
    </svg>
  )
}
