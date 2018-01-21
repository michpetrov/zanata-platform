// @ts-check
import React from 'react'
import PropTypes from 'prop-types'
import Loading from 'react-loading'

interface LoaderProps {
    className: string
}

const Loader: React.SFC<LoaderProps> = (props) => {
  return (
    <span className={props.className}>
      <span>
        <Loading type='bubbles' color='#546677' />
      </span>
    </span>
  )
}

export default Loader
