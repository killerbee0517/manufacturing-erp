import React from 'react'
import { Link, Route, Routes, useNavigate } from 'react-router-dom'
import {
  CContainer,
  CHeader,
  CHeaderBrand,
  CHeaderNav,
  CNavItem,
  CNavLink,
  CSidebar,
  CSidebarNav,
  CNavTitle,
  CNavGroup,
  CButton
} from '@coreui/react'
import DashboardPage from '../pages/DashboardPage.jsx'
import SimpleListPage from '../pages/SimpleListPage.jsx'
import StockTransferPage from '../pages/StockTransferPage.jsx'

const DefaultLayout = () => {
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem('token')
    navigate('/login')
  }

  return (
    <div className="d-flex">
      <CSidebar unfoldable visible className="border-end">
        <CHeaderBrand className="p-3">Manufacturing ERP</CHeaderBrand>
        <CSidebarNav>
          <CNavItem>
            <CNavLink component={Link} to="/dashboard">Dashboard</CNavLink>
          </CNavItem>
          <CNavTitle>Masters</CNavTitle>
          <CNavGroup toggler="Masters">
            <CNavItem>
              <CNavLink component={Link} to="/masters/suppliers">Suppliers</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink component={Link} to="/masters/items">Items</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink component={Link} to="/masters/locations">Locations</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink component={Link} to="/masters/vehicles">Vehicles</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink component={Link} to="/masters/brokers">Brokers</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink component={Link} to="/masters/users">Users & Roles</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink component={Link} to="/masters/tds-rules">TDS Rules</CNavLink>
            </CNavItem>
          </CNavGroup>
          <CNavTitle>Purchase</CNavTitle>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/rfq">RFQ</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/po">Purchase Orders</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/weighbridge-in">Weighbridge In</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/grn">GRN</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/qc">QC Inspection</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/invoices">Purchase Invoices</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/purchase/debit-notes">Debit Notes</CNavLink>
          </CNavItem>
          <CNavTitle>Sales</CNavTitle>
          <CNavItem>
            <CNavLink component={Link} to="/sales/so">Sales Orders</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/sales/weighbridge-out">Weighbridge Out</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/sales/delivery">Delivery</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/sales/invoices">Sales Invoices</CNavLink>
          </CNavItem>
          <CNavTitle>Inventory</CNavTitle>
          <CNavItem>
            <CNavLink component={Link} to="/inventory/stock-on-hand">Stock On Hand</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/inventory/stock-ledger">Stock Ledger</CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink component={Link} to="/inventory/stock-transfer">Stock Transfer</CNavLink>
          </CNavItem>
          <CNavTitle>Reports</CNavTitle>
          <CNavItem>
            <CNavLink component={Link} to="/reports/tds">TDS Register</CNavLink>
          </CNavItem>
        </CSidebarNav>
      </CSidebar>

      <div className="flex-grow-1">
        <CHeader className="border-bottom">
          <CContainer fluid>
            <CHeaderBrand>Manufacturing ERP</CHeaderBrand>
            <CHeaderNav className="ms-auto">
              <CNavItem>
                <CNavLink onClick={handleLogout}>Logout</CNavLink>
              </CNavItem>
            </CHeaderNav>
          </CContainer>
        </CHeader>

        <CContainer className="py-4">
          <Routes>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/masters/:section" element={<SimpleListPage title="Masters" />} />
            <Route path="/purchase/:section" element={<SimpleListPage title="Purchase" />} />
            <Route path="/sales/:section" element={<SimpleListPage title="Sales" />} />
            <Route path="/inventory/stock-transfer" element={<StockTransferPage />} />
            <Route path="/inventory/:section" element={<SimpleListPage title="Inventory" />} />
            <Route path="/reports/:section" element={<SimpleListPage title="Reports" />} />
            <Route path="*" element={<DashboardPage />} />
          </Routes>
        </CContainer>
      </div>
    </div>
  )
}

export default DefaultLayout
