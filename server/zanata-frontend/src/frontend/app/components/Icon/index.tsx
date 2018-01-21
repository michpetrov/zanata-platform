import React from 'react';
import PropTypes from 'prop-types';
const Icon = ({ name, className, ...props }) => {
    const svgIcon = `<use xlink:href="#Icon-${name}" />`;
    return (<span {...props}>
      <svg dangerouslySetInnerHTML={{ __html: svgIcon }} className={className} style={{ fill: 'currentColor' }}/></span>);
};
export default Icon;
