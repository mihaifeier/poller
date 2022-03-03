import { TextField } from "@mui/material";
import { KeyboardEvent, useState } from "react";
import { useNavigate } from 'react-router-dom';
import "./Login.css";

const Login = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState("");

    const login = (event: KeyboardEvent<HTMLDivElement>) => {
        if (event.key === "Enter" && user.length > 0) {
            localStorage.setItem("user", user);
            navigate("/");
        }
    }

    return (
        <div className="login-box-wrapper">
            <div className="login-box">
                <TextField
                    sx={{ width: "100%" }}
                    className="login-text-field"
                    label="User"
                    variant="outlined"
                    onKeyDown={login}
                    onChange={(event) => setUser(event.target.value)}
                />
                <div className="login-info">Please enter a name and press enter.</div>
            </div>
        </div>
    );
}

export default Login;
