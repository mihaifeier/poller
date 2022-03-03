import { createTheme, ThemeProvider } from '@mui/material';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import "./App.css";
import Login from './component/Login';
import Menu from './component/Menu';
import PollInfo from './component/PollInfo';


const App = () => {
    const location = useLocation();
    const darkTheme = createTheme({
        palette: {
            mode: 'dark',
        },
    });

    if (localStorage.getItem("user") === null && location.pathname !== "/login") {
        return <Navigate to="/login"/>
    } else if (localStorage.getItem("user") !== null && location.pathname === "/login") {
        return <Navigate to="/"/>
    }

    return <ThemeProvider theme={darkTheme}>
        <div className="App">
            {localStorage.getItem("user") ? <Menu /> : null}
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<PollInfo />} />
            </Routes>
        </div>
    </ThemeProvider>;
}

export default App;
