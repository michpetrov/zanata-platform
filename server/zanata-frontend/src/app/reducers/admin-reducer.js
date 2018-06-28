import {handleActions} from 'redux-actions'
import update from 'immutability-helper'
import {
  GET_ALL_CRITERIA_SUCCESS,
  ADD_CRITERION_SUCCESS,
  EDIT_CRITERION_SUCCESS,
  DELETE_CRITERION_SUCCESS,
  ADD_CRITERION_FAILURE,
  EDIT_CRITERION_FAILURE,
  DELETE_CRITERION_FAILURE,
  GET_ALL_CRITERIA_FAILURE
} from '../actions/review-actions'
import { SEVERITY } from '../actions/common-actions'

const defaultState = {
  notification: undefined,
  review: {
    criteria: []
  }
}

// selectors
// @ts-ignore any
const getCriteria = state => state.review.criteria

export const selectors = {
  getCriteria,
  // @ts-ignore any
  getNotification: state => state.notification
}

// utility function
// @ts-ignore any
const getErrorMessage = action => {
  if (action.error) {
    return action.payload && action.payload.message
  }
  return undefined
}

const admin = handleActions({
  [GET_ALL_CRITERIA_SUCCESS]: (state, action) => {
    return update(state, {
      review: { criteria: { $set: action.payload } }
    })
  },
  [ADD_CRITERION_SUCCESS]: (state, action) => {
    return update(state, {
      review: { criteria: { $push: [action.payload] } }
    })
  },
  [EDIT_CRITERION_SUCCESS]: (state, action) => {
    const index = state.review.criteria
      // @ts-ignore
      .findIndex(c => c.id === action.payload.id)
    if (index >= 0) {
      return update(state, {
        review: { criteria: { [index]: { $set: action.payload } } }
      })
    }
    return state
  },
  [DELETE_CRITERION_SUCCESS]: (state, action) => {
    const index = state.review.criteria
      // @ts-ignore
      .findIndex(c => c.id === action.payload.id)
    if (index >= 0) {
      return update(state, {
        review: { criteria: { $splice: [[index, 1]] } }
      })
    }
    return state
  },
  [ADD_CRITERION_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Add Criteria failed.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [EDIT_CRITERION_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Edit criteria failed.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [DELETE_CRITERION_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Delete Criteria failed.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [GET_ALL_CRITERIA_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Failed to retrieve review criteria.`,
          description: getErrorMessage(action)
        }
      }
    })
  }
}, defaultState)

export default admin
