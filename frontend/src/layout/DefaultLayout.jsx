import React, { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  CContainer,
  CFooter,
  CHeader,
  CHeaderBrand,
  CHeaderNav,
  CHeaderToggler,
  CNavItem,
  CNavLink,
  CSidebar,
  CSidebarBrand,
  CSidebarNav
} from '@coreui/react'
import SimpleBar from 'simplebar-react'
import AppSidebarNav from '../components/AppSidebarNav.jsx'
import navigation from '../_nav.tsx'

const DefaultLayout = () => {
  const [sidebarVisible, setSidebarVisible] = useState(true)
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem('token')
    navigate('/login')
  }

  return (
    <div className="d-flex min-vh-100 bg-light">
      <CSidebar
        className="border-end"
        unfoldable
        visible={sidebarVisible}
        onVisibleChange={(value) => setSidebarVisible(value)}
      >
        <CSidebarBrand className="d-flex align-items-center px-3 py-2">
          <span className="fw-semibold">Manufacturing ERP</span>
        </CSidebarBrand>
        <CSidebarNav>
          <SimpleBar className="h-100">
            <AppSidebarNav items={navigation} />
          </SimpleBar>
        </CSidebarNav>
      </CSidebar>

      <div className="wrapper d-flex flex-column flex-grow-1 min-vh-100">
        <CHeader className="border-bottom">
          <CContainer fluid className="d-flex align-items-center gap-3">
            <CHeaderToggler
              onClick={() => setSidebarVisible((value) => !value)}
              className="ps-1"
            />
            <CHeaderBrand className="me-auto">Dashboard</CHeaderBrand>
            <CHeaderNav className="ms-auto">
              <CNavItem>
                <CNavLink as={NavLink} to="/dashboard">Home</CNavLink>
              </CNavItem>
              <CNavItem>
                <CNavLink role="button" onClick={handleLogout}>Logout</CNavLink>
              </CNavItem>
            </CHeaderNav>
          </CContainer>
        </CHeader>

        <div className="body flex-grow-1">
          <CContainer fluid className="py-4">
            <Outlet />
          </CContainer>
        </div>

        <CFooter className="px-4">
          <div>Manufacturing ERP</div>
          <div className="ms-auto">Powered by CoreUI</div>
        </CFooter>
      </div>
    </div>
  )
}

export default DefaultLayout
