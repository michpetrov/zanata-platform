import React from 'react'
import { connect, GenericDispatch } from 'react-redux'
import Helmet from 'react-helmet'

export interface Props {
  handleSaveChanges: typeof Function
}

@connect(mapStateToProps, mapDispatchToProps)
export class ServerSettings extends React.Component<Props, {}> {
  public render() {
    return (
      <div>
        <Helmet title='Server Settings' />
          <div className='page wideView' id='ServerSettings'>
            <div>
                <label htmlFor='url'>Server URL</label>
                <input id='url' placeholder='http://zanata.example.com'></input>
            </div>

            <div>
              <label htmlFor='registerUrl'>Register URL</label>
              <input id='registerUrl' placeholder='http://example.com/register'></input>
            </div>

            <div>
              <label htmlFor='emailDomain'>Email Domain Name</label>
              <input id='emailDomain' placeholder='redhat.com'></input>
            </div>

            <div>
              <label htmlFor='adminEmails'>Contact Admin Address</label>
              <input id='adminEmails' placeholder='username@domain.com (comma separated for multiple)'></input>
            </div>

            <div>
              <label htmlFor='permittedEmails'>Permitted user email domain</label>
              <input id='permittedEmails' placeholder='username@domain.com (comma separated for multiple)'></input>
            </div>

            <div>
              <label htmlFor='fromEmail'>From Email Address</label>
              <input id='fromEmail' placeholder='username@domain.com'></input>
            </div>

            <div>
              <label htmlFor='termsURL'>Terms of Use URL</label>
              <input id='termsURL' placeholder='Default to http://zanata.org/terms'></input>
            </div>

            <div>
              <label htmlFor='helpURL'>Help URL</label>
              <input id='helpURL' placeholder='Default to http://docs.zanata.org/en/release/'></input>
            </div>
          </div>

      </div>
    )
  }
}

function mapStateToProps() {
  return {
  };
}

function mapDispatchToProps(dispatch: GenericDispatch) {
  return {
    handleSaveChanges: () => dispatch(console.error('aeng'))
  }
}
