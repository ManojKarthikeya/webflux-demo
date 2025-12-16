import React from 'react';
import { Box, Container, CssBaseline } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Navigation from './Navigation';

const Layout: React.FC = () => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <CssBaseline />
      <Navigation />
      <Container component="main" sx={{ mt: 4, mb: 4, flexGrow: 1 }}>
        <Outlet />
      </Container>
    </Box>
  );
};

export default Layout;
