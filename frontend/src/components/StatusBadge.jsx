import React from 'react'
import { CBadge } from '@coreui/react'

const statusColor = {
  DRAFT: 'secondary',
  APPROVED: 'info',
  RELEASED: 'primary',
  POSTED: 'success',
  CANCELLED: 'danger',
  PENDING: 'warning',
  HOLD: 'warning',
  ACCEPTED: 'success',
  REJECTED: 'danger'
}

const StatusBadge = ({ status }) => (
  <CBadge color={statusColor[status] || 'secondary'}>{status}</CBadge>
)

export default StatusBadge
