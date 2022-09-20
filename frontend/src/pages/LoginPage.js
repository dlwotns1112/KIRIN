import React from 'react';
import { Avatar, Button, TextField, Box, Grid, Link, Typography, Container } from '@mui/material/';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import LoginTop from '../components/sign/LoginTop';
import styles from './LoginPage.module.css';

const theme = createTheme({
  palette: {
    primary: {
      main: '#FFC947',
    },
    secondary: {
      main: '#11cb5f',
    },
  },
});

function LoginPage() {
  return (
    <ThemeProvider theme={theme}>
      <LoginTop styles={styles}></LoginTop>
      <Container component="main" maxWidth="xs">
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}></Avatar>
          <Typography component="h1" variant="h5"></Typography>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="이메일"
            name="email"
            autoComplete="email"
            autoFocus
            size="small"
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="비밀번호"
            type="password"
            id="password"
            autoComplete="current-password"
            size="small"
          />

          <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 1 }}>
            로그인
          </Button>
          <Grid container>
            <Grid item xs>
              <Link href="/findpassword" variant="body2">
                비밀번호 찾기
              </Link>
            </Grid>
            <Grid item>
              <Link href="/signup" variant="body2">
                {'회원가입'}
              </Link>
            </Grid>
          </Grid>
        </Box>
      </Container>
    </ThemeProvider>
  );
}

export default LoginPage;
