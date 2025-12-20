import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { CButton, CCard, CCardBody, CCardHeader, CForm, CFormInput } from '@coreui/react'
import api from '../services/api'

const schema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required')
})

const LoginPage = () => {
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: zodResolver(schema)
  })

  const onSubmit = async (values) => {
    const response = await api.post('/api/auth/login', values)
    localStorage.setItem('token', response.data.token)
    navigate('/dashboard')
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <CCard style={{ width: 360 }}>
        <CCardHeader>ERP Login</CCardHeader>
        <CCardBody>
          <CForm onSubmit={handleSubmit(onSubmit)}>
            <CFormInput
              className="mb-3"
              label="Username"
              placeholder="admin"
              feedbackInvalid={errors.username?.message}
              invalid={!!errors.username}
              {...register('username')}
            />
            <CFormInput
              className="mb-3"
              type="password"
              label="Password"
              placeholder="admin123"
              feedbackInvalid={errors.password?.message}
              invalid={!!errors.password}
              {...register('password')}
            />
            <CButton type="submit" color="primary" disabled={isSubmitting} className="w-100">
              {isSubmitting ? 'Signing in...' : 'Login'}
            </CButton>
          </CForm>
        </CCardBody>
      </CCard>
    </div>
  )
}

export default LoginPage
