import React from 'react'
import { NavLink } from 'react-router-dom'
import { CNavGroup, CNavItem, CNavLink, CNavTitle } from '@coreui/react'

const AppSidebarNav = ({ items }) => {
  const renderNavLink = (name, icon) => (
    <>
      {icon}
      <span>{name}</span>
    </>
  )

  const renderItem = (item, index) => {
    if (item.component === CNavTitle) {
      return (
        <CNavTitle key={`${item.name}-${index}`}>
          {item.name}
        </CNavTitle>
      )
    }

    if (item.component === CNavGroup) {
      return (
        <CNavGroup
          key={`${item.name}-${index}`}
          toggler={renderNavLink(item.name, item.icon)}
        >
          {item.items?.map((child, childIndex) => renderItem(child, `${index}-${childIndex}`))}
        </CNavGroup>
      )
    }

    return (
      <CNavItem key={`${item.name}-${index}`}>
        <CNavLink as={NavLink} to={item.to} end={item.end}>
          {renderNavLink(item.name, item.icon)}
        </CNavLink>
      </CNavItem>
    )
  }

  return <>{items.map((item, index) => renderItem(item, index))}</>
}

export default AppSidebarNav
