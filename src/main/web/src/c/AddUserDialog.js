import React, {Component} from 'react'
import {TextValidator, ValidatorForm} from 'react-material-ui-form-validator'
import DialogTitle from '@material-ui/core/DialogTitle/DialogTitle'
import DialogContent from '@material-ui/core/DialogContent/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogActions from '@material-ui/core/DialogActions/DialogActions'
import Fab from '@material-ui/core/Fab'
import PersonAddIcon from '@material-ui/icons/PersonAdd'
import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog/Dialog'
import {AddUserUrl} from './GmhUrl'
import axios from 'axios'

class AddUserDialog extends Component{

    constructor(props) {
        super(props)
        this.state = {
	    openAddDialog: false,
	    errorMsg: '',
            credentials : {
                login: '',
                password: '',
                email: ''
            }
        }
    }

    addUser = () => {
        let url = AddUserUrl()
        axios.post(url,{
            login: this.state.credentials.login,
            email: this.state.credentials.email,
            password: this.state.credentials.password
        }).then( res => {
            this.props.reload()
	    this.hideDialog()
        }, (error) => {
            this.setState({
                // data: error,
                // url: url,
                // error: true
		errorMsg: error.toString()
            })
        })
    }

    openDialog = () => {
	    console.log('===open')
        this.setState({
            openAddDialog: true
        })
    }

    hideDialog = () => {
        this.setState({
            openAddDialog: false
        })
    }

    handleChange = (event) => {
        let credentials = this.state.credentials
        credentials[event.target.name] = event.target.value
        this.setState({
            credentials : credentials
        })
    }


    render() {
	    const cChildren = React.Children.map(this.props.children, (el, idx) => {
		    return React.cloneElement(el, { onClick: this.openDialog })
	    })

	return (
	<>
	{/*
		<Fab variant="extended" onClick={this.openDialog} style={{float:'right'}}>
			<PersonAddIcon/>
			Add User
		</Fab>
	*/}
	{/*
	<div onClick={this.openDialog}>
	{this.props.children}
	</div>
	*/}
		{cChildren}
                <Dialog open={this.state.openAddDialog} onClose={this.hideDialog} aria-labelledby="form-dialog-title">
		    <div>{this.state.errorMsg}</div>

                    <ValidatorForm onSubmit={this.addUser}>
                        <DialogTitle id="form-dialog-title">Add User</DialogTitle>
                        <DialogContent>
                            <DialogContentText>
                                Enter User Details :
                            </DialogContentText>
                            <TextValidator
                                label="Email"
                                onChange={this.handleChange}
                                name="email"
                                type="email"
                                variant="outlined"
                                inputProps={{ maxLength: 60 }}
                                validators={['required']}
                                errorMessages={['this field is required']}
                                value={this.state.credentials.email}/>
                            <br/>
                            <TextValidator
                                label="Login"
                                onChange={this.handleChange}
                                name="login"
                                type="text"
                                variant="outlined"
                                inputProps={{ maxLength: 60 }}
                                validators={['required']}
                                errorMessages={['this field is required']}
                                value={this.state.credentials.login}/>
                            <br/>
                            <TextValidator
                                label="Password"
                                onChange={this.handleChange}
                                name="password"
                                type="password"
                                variant="outlined"
                                inputProps={{ maxLength: 60 }}
                                validators={['required']}
                                errorMessages={['this field is required']}
                                value={this.state.credentials.password}/>

                        </DialogContent>
                        <DialogActions>
                            <Button onClick={this.hideDialog} color="primary">
                                Cancel
                            </Button>
                            <Button type="submit" color="primary">
                                Add
                            </Button>
                        </DialogActions>
                    </ValidatorForm>
                </Dialog>
	</>
        )
    }
}

export default AddUserDialog
