import React from 'react'
import { useForm } from 'react-hook-form'
import { CCard, CCardBody, CCardHeader, CForm, CFormInput, CButton } from '@coreui/react'
import api from '../services/api'

const StockTransferPage = () => {
  const {
    register,
    handleSubmit,
    reset
  } = useForm()

  const onSubmit = async (values) => {
    await api.post('/api/stock-transfers', {
      itemId: Number(values.itemId),
      fromLocationId: Number(values.fromLocationId),
      toLocationId: Number(values.toLocationId),
      quantity: Number(values.quantity),
      weight: Number(values.weight)
    })
    reset()
  }

  return (
    <CCard>
      <CCardHeader>Stock Transfer</CCardHeader>
      <CCardBody>
        <CForm onSubmit={handleSubmit(onSubmit)}>
          <CFormInput className="mb-3" label="Item ID" {...register('itemId')} />
          <CFormInput className="mb-3" label="From Location ID" {...register('fromLocationId')} />
          <CFormInput className="mb-3" label="To Location ID" {...register('toLocationId')} />
          <CFormInput className="mb-3" label="Quantity" {...register('quantity')} />
          <CFormInput className="mb-3" label="Weight" {...register('weight')} />
          <CButton type="submit" color="primary">Transfer</CButton>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default StockTransferPage
