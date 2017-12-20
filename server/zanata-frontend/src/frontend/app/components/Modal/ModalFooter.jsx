// @ts-check
import React from 'react'
import PropTypes from 'prop-types'

/**
 * @type { React.StatelessComponent<{children, props?}> }
 */
const ModalFooter = ({
  children,
  ...props
}) => {
  return (
    <div className='modal-footer'
      {...props}
    >
      {children}
    </div>
  )
}

ModalFooter.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalFooter
