import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import EventList from './pages/EventList';
import EventDetail from './pages/EventDetail';
import UserCenter from './pages/UserCenter';
import Payment from './pages/Payment';
import RefundRequest from './pages/RefundRequest';

const { Header, Content, Footer } = Layout;

const App = () => {
  return (
    <Router>
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ 
          position: 'fixed', 
          zIndex: 1, 
          width: '100%',
          background: '#fff',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}>
          <div style={{
            float: 'left',
            width: '120px',
            height: '31px',
            margin: '16px 24px 16px 0',
            background: '#1890ff',
            borderRadius: '4px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 'bold'
          }}>
            票务系统
          </div>
          <Menu
            theme="light"
            mode="horizontal"
            defaultSelectedKeys={['1']}
            items={[
              {
                key: '1',
                label: '演出活动',
              },
              {
                key: '2',
                label: '我的订单',
              },
            ]}
          />
        </Header>
        <Content style={{ padding: '0 50px', marginTop: 64 }}>
          <Routes>
            <Route path="/" element={<EventList />} />
            <Route path="/event/:eventId" element={<EventDetail />} />
            <Route path="/user/*" element={<UserCenter />} />
            <Route path="/payment/:orderId" element={<Payment />} />
            <Route path="/refund/:orderId" element={<RefundRequest />} />
          </Routes>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          票务系统 ©{new Date().getFullYear()} Created by Your Company
        </Footer>
      </Layout>
    </Router>
  );
};

export default App; 