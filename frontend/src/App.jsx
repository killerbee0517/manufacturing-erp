import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import DefaultLayout from './layout/DefaultLayout.jsx'
import LoginPage from './pages/LoginPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import SimpleListPage from './pages/SimpleListPage.jsx'
import StockTransferPage from './pages/StockTransferPage.jsx'

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token')
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return children
}

const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route
      path="/*"
      element={
        <ProtectedRoute>
          <DefaultLayout />
        </ProtectedRoute>
      }
    />
  </Routes>
)

export default App
